package com.datasift.dropwizard.zookeeper;

import com.datasift.dropwizard.zookeeper.config.CuratorConfiguration;
import com.datasift.dropwizard.zookeeper.config.ZooKeeperConfiguration;
import com.datasift.dropwizard.zookeeper.ensemble.DropwizardConfiguredEnsembleProvider;
import com.datasift.dropwizard.zookeeper.health.CuratorHealthCheck;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.yammer.dropwizard.config.Environment;

/**
 * A factory for creating and managing {@link CuratorFramework} instances.
 * <p>
 * The resulting {@link CuratorFramework} will have its lifecycle managed by the
 * {@link Environment} and will have {@link com.yammer.metrics.core.HealthCheck}s
 * installed for the underlying ZooKeeper ensemble.
 *
 * @see CuratorFramework
 */
public class CuratorFactory {

    private final Environment environment;

    /**
     * Creates a new {@link CuratorFactory} instance for the given {@link
     * Environment}.
     *
     * @param environment the {@link Environment} instance to build {@link
     *                    CuratorFramework} instances for.
     */
    public CuratorFactory(final Environment environment) {
        this.environment = environment;
    }

    /**
     * Builds a default {@link CuratorFramework} instance from the given {@link
     * CuratorConfiguration}.
     *
     * @param configuration the {@link CuratorConfiguration} for the ensemble
     *                      to configure the {@link CuratorFramework} instance
     *                      for.
     * @return a {@link CuratorFramework} instance, managed and configured
     *         according to the {@code configuration}.
     */
    public CuratorFramework build(final CuratorConfiguration configuration) {
        return build(configuration, "default");
    }

    /**
     * Builds a {@link CuratorFramework} instance from the given {@link
     * CuratorConfiguration} with the given {@code name}.
     *
     * @param configuration the {@link CuratorConfiguration} for the ensemble
     *                      to configure the {@link CuratorFramework} instance
     *                      for.
     * @param name the name for the {@link CuratorFramework} instance.
     * @return a {@link CuratorFramework} instance, managed and configured
     *         according to the {@code configuration}.
     */
    public CuratorFramework build(final CuratorConfiguration configuration,
                                  final String name) {
        final ZooKeeperConfiguration zkConfiguration = configuration.getEnsembleConfiguration();
        final CuratorFramework framework = CuratorFrameworkFactory.builder()
                .ensembleProvider(new DropwizardConfiguredEnsembleProvider(zkConfiguration))
                .connectionTimeoutMs((int) zkConfiguration.getConnectionTimeout().toMilliseconds())
                .sessionTimeoutMs((int) zkConfiguration.getSessionTimeout().toMilliseconds())
                .namespace(zkConfiguration.getNamespace().toString())
                .retryPolicy(configuration.getRetryPolicy())
                .build();

        environment.addHealthCheck(new CuratorHealthCheck(framework, name));
        environment.manage(new ManagedCuratorFramework(framework));

        return framework;
    }

}
