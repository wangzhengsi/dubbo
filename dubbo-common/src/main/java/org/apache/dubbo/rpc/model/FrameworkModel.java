/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.rpc.model;

import org.apache.dubbo.common.config.Environment;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.extension.ExtensionScope;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.resource.GlobalResourcesRepository;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.metadata.definition.TypeDefinitionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Model of dubbo framework, it can be shared with multiple applications.
 */
public class FrameworkModel extends ScopeModel {

    // ========================= Static Fields Start ===================================

    protected static final Logger LOGGER = LoggerFactory.getLogger(FrameworkModel.class);

    public static final String NAME = "FrameworkModel";
    private static final AtomicLong index = new AtomicLong(1);

    private static final Object globalLock = new Object();

    private static volatile FrameworkModel defaultInstance;

    /**
     * 实例对象集合
     */
    private static final List<FrameworkModel> allInstances = new CopyOnWriteArrayList<>();

    // ========================= Static Fields End ===================================

    // internal app index is 0, default app index is 1
    private final AtomicLong appIndex = new AtomicLong(0);

    private volatile ApplicationModel defaultAppModel;

    /**
     * 所有applicationModel实例对象集合
     */
    private final List<ApplicationModel> applicationModels = new CopyOnWriteArrayList<>();

    /**
     * 公开applicationModel实例对象集合
     */
    private final List<ApplicationModel> pubApplicationModels = new CopyOnWriteArrayList<>();

    /**
     * 框架的服务存储库
     */
    private final FrameworkServiceRepository serviceRepository;

    /**
     * 内部应用程序模型对象
     */
    private final ApplicationModel internalApplicationModel;

    private final ReentrantLock destroyLock = new ReentrantLock();

    /**
     * Use {@link FrameworkModel#newModel()} to create a new model
     */
    public FrameworkModel() {
        // 第一个参数为null代表是顶层模型
        // 第二个参数代表是FRAMEWORK域
        // 第三个参数代表不是内部域
        super(null, ExtensionScope.FRAMEWORK, false);
        synchronized (globalLock) {
            synchronized (instLock) {
                // id是自增的
                this.setInternalId(String.valueOf(index.getAndIncrement()));
                // 将框架模型添加到容器中
                // register FrameworkModel instance early
                allInstances.add(this);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(getDesc() + " is created");
                }
                // 父类ScopeModel初始化
                initialize();

                // 初始化类型构建器，用于支持泛型
                TypeDefinitionBuilder.initBuilders(this);

                // 框架服务存储仓库对象，用来快速查询服务提供者信息
                serviceRepository = new FrameworkServiceRepository(this);

                // 基于Dubbo SPI机制加载ScopeModelInitializer扩展点
                ExtensionLoader<ScopeModelInitializer> initializerExtensionLoader =
                        this.getExtensionLoader(ScopeModelInitializer.class);
                // 获取ScopeModelInitializer类型的支持的扩展集合
                Set<ScopeModelInitializer> initializers = initializerExtensionLoader.getSupportedExtensionInstances();
                // 遍历这些扩展来调用初始化方法
                for (ScopeModelInitializer initializer : initializers) {
                    initializer.initializeFrameworkModel(this);
                }

                // 创建一个内部ApplicationModel
                internalApplicationModel = new ApplicationModel(this, true);
                // 注册ApplicationConfig
                internalApplicationModel
                        .getApplicationConfigManager()
                        .setApplication(new ApplicationConfig(
                                internalApplicationModel, CommonConstants.DUBBO_INTERNAL_APPLICATION));
                // 设置内部ApplicationModel名字为DUBBO_INTERNAL_APPLICATION
                internalApplicationModel.setModelName(CommonConstants.DUBBO_INTERNAL_APPLICATION);
            }
        }
    }

    @Override
    protected void onDestroy() {
        synchronized (instLock) {
            if (defaultInstance == this) {
                // NOTE: During destroying the default FrameworkModel, the FrameworkModel.defaultModel() or
                // ApplicationModel.defaultModel()
                // will return a broken model, maybe cause unpredictable problem.
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Destroying default framework model: " + getDesc());
                }
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(getDesc() + " is destroying ...");
            }

            // destroy all application model
            for (ApplicationModel applicationModel : new ArrayList<>(applicationModels)) {
                applicationModel.destroy();
            }
            // check whether all application models are destroyed
            checkApplicationDestroy();

            // notify destroy and clean framework resources
            // see org.apache.dubbo.config.deploy.FrameworkModelCleaner
            notifyDestroy();

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(getDesc() + " is destroyed");
            }

            // remove from allInstances and reset default FrameworkModel
            synchronized (globalLock) {
                allInstances.remove(this);
                resetDefaultFrameworkModel();
            }

            // if all FrameworkModels are destroyed, clean global static resources, shutdown dubbo completely
            destroyGlobalResources();
        }
    }

    private void checkApplicationDestroy() {
        synchronized (instLock) {
            if (applicationModels.size() > 0) {
                List<String> remainApplications =
                        applicationModels.stream().map(ScopeModel::getDesc).collect(Collectors.toList());
                throw new IllegalStateException(
                        "Not all application models are completely destroyed, remaining " + remainApplications.size()
                                + " application models may be created during destruction: " + remainApplications);
            }
        }
    }

    private void destroyGlobalResources() {
        synchronized (globalLock) {
            if (allInstances.isEmpty()) {
                GlobalResourcesRepository.getInstance().destroy();
            }
        }
    }

    /**
     * During destroying the default FrameworkModel, the FrameworkModel.defaultModel() or ApplicationModel.defaultModel()
     * will return a broken model, maybe cause unpredictable problem.
     * Recommendation: Avoid using the default model as much as possible.
     * @return the global default FrameworkModel
     */
    public static FrameworkModel defaultModel() {
        // 单例
        FrameworkModel instance = defaultInstance;
        if (instance == null) {
            synchronized (globalLock) {
                // 重置默认模型
                resetDefaultFrameworkModel();
                if (defaultInstance == null) {
                    defaultInstance = new FrameworkModel();
                }
                instance = defaultInstance;
            }
        }
        Assert.notNull(instance, "Default FrameworkModel is null");
        return instance;
    }

    /**
     * Get all framework model instances
     * @return
     */
    public static List<FrameworkModel> getAllInstances() {
        synchronized (globalLock) {
            return Collections.unmodifiableList(new ArrayList<>(allInstances));
        }
    }

    /**
     * Destroy all framework model instances, shutdown dubbo engine completely.
     */
    public static void destroyAll() {
        synchronized (globalLock) {
            for (FrameworkModel frameworkModel : new ArrayList<>(allInstances)) {
                frameworkModel.destroy();
            }
        }
    }

    public ApplicationModel newApplication() {
        synchronized (instLock) {
            return new ApplicationModel(this);
        }
    }

    /**
     * Get or create default application model
     * @return
     */
    public ApplicationModel defaultApplication() {
        ApplicationModel appModel = this.defaultAppModel;
        if (appModel == null) {
            // check destroyed before acquire inst lock, avoid blocking during destroying
            checkDestroyed();
            resetDefaultAppModel();
            if ((appModel = this.defaultAppModel) == null) {
                synchronized (instLock) {
                    if (this.defaultAppModel == null) {
                        this.defaultAppModel = newApplication();
                    }
                    appModel = this.defaultAppModel;
                }
            }
        }
        Assert.notNull(appModel, "Default ApplicationModel is null");
        return appModel;
    }

    ApplicationModel getDefaultAppModel() {
        return defaultAppModel;
    }

    void addApplication(ApplicationModel applicationModel) {
        // 如果FrameWorkModel已经标记为销毁则抛出异常
        // can not add new application if it's destroying
        checkDestroyed();
        synchronized (instLock) {
            if (!this.applicationModels.contains(applicationModel)) {
                // 为applicationModel生成内部id
                applicationModel.setInternalId(buildInternalId(getInternalId(), appIndex.getAndIncrement()));
                // 添加到集合中
                this.applicationModels.add(applicationModel);
                // 如果非内部应用模型则添加到公开应用模型集合中
                if (!applicationModel.isInternal()) {
                    this.pubApplicationModels.add(applicationModel);
                }
            }
        }
    }

    void removeApplication(ApplicationModel model) {
        synchronized (instLock) {
            this.applicationModels.remove(model);
            if (!model.isInternal()) {
                this.pubApplicationModels.remove(model);
            }
            resetDefaultAppModel();
        }
    }

    /**
     * Protocols are special resources that need to be destroyed as soon as possible.
     *
     * Since connections inside protocol are not classified by applications, trying to destroy protocols in advance might only work for singleton application scenario.
     */
    void tryDestroyProtocols() {
        synchronized (instLock) {
            if (pubApplicationModels.size() == 0) {
                notifyProtocolDestroy();
            }
        }
    }

    void tryDestroy() {
        synchronized (instLock) {
            if (pubApplicationModels.size() == 0) {
                destroy();
            }
        }
    }

    private void checkDestroyed() {
        if (isDestroyed()) {
            throw new IllegalStateException("FrameworkModel is destroyed");
        }
    }

    private void resetDefaultAppModel() {
        synchronized (instLock) {
            if (this.defaultAppModel != null && !this.defaultAppModel.isDestroyed()) {
                return;
            }
            ApplicationModel oldDefaultAppModel = this.defaultAppModel;
            if (pubApplicationModels.size() > 0) {
                this.defaultAppModel = pubApplicationModels.get(0);
            } else {
                this.defaultAppModel = null;
            }
            if (defaultInstance == this && oldDefaultAppModel != this.defaultAppModel) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Reset global default application from " + safeGetModelDesc(oldDefaultAppModel) + " to "
                            + safeGetModelDesc(this.defaultAppModel));
                }
            }
        }
    }

    private static void resetDefaultFrameworkModel() {
        // 同一时刻只能有一个线程执行操作
        synchronized (globalLock) {
            // 如果实例已经销毁则直接返回
            if (defaultInstance != null && !defaultInstance.isDestroyed()) {
                return;
            }
            FrameworkModel oldDefaultFrameworkModel = defaultInstance;
            // FrameworkModel有多个则取第一个作为默认的
            if (allInstances.size() > 0) {
                defaultInstance = allInstances.get(0);
            } else {
                defaultInstance = null;
            }
            if (oldDefaultFrameworkModel != defaultInstance) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Reset global default framework from " + safeGetModelDesc(oldDefaultFrameworkModel)
                            + " to " + safeGetModelDesc(defaultInstance));
                }
            }
        }
    }

    private static String safeGetModelDesc(ScopeModel scopeModel) {
        return scopeModel != null ? scopeModel.getDesc() : null;
    }

    /**
     * Get all application models except for the internal application model.
     */
    public List<ApplicationModel> getApplicationModels() {
        synchronized (globalLock) {
            return Collections.unmodifiableList(pubApplicationModels);
        }
    }

    /**
     * Get all application models including the internal application model.
     */
    public List<ApplicationModel> getAllApplicationModels() {
        synchronized (globalLock) {
            return Collections.unmodifiableList(applicationModels);
        }
    }

    public ApplicationModel getInternalApplicationModel() {
        return internalApplicationModel;
    }

    public FrameworkServiceRepository getServiceRepository() {
        return serviceRepository;
    }

    @Override
    protected Lock acquireDestroyLock() {
        return destroyLock;
    }

    @Override
    public Environment modelEnvironment() {
        throw new UnsupportedOperationException("Environment is inaccessible for FrameworkModel");
    }

    @Override
    protected boolean checkIfClassLoaderCanRemoved(ClassLoader classLoader) {
        return super.checkIfClassLoaderCanRemoved(classLoader)
                && applicationModels.stream()
                        .noneMatch(applicationModel -> applicationModel.containsClassLoader(classLoader));
    }
}
