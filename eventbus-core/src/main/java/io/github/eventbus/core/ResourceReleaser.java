package io.github.eventbus.core;

import io.github.ali.commons.variable.MixedActionGenerator;

/**
 * 资源释放器<br/>
 * 框架启动时申请的各种资源由该类负责释放
 * @author ALi
 * @version 1.0
 * @date 2022-09-14 10:55
 * @description
 */
public class ResourceReleaser {
    public ResourceReleaser() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                MixedActionGenerator.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
