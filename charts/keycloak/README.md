# SSO Keycloak Helm Chart

The `SSO Keycloak Helm Chart` provides a easy way to deploy (RedHat SSO)[https://access.redhat.com/products/red-hat-single-sign-on], which is specifically designed for BCGov SSO services, on Openshift.

## Usages

### Add this chart repository

```console
$ helm repo add sso-keycloak https://bcgov.github.io/sso-keycloak
```

### Install this chart repository

```console
$ helm install <release-name> sso-keycloak/sso-keycloak [--namespace <my-namespace>] [--version <x.y.z>] [--values ./custom-values.yaml]
```

### Upgrade this chart repository

```console
$ helm upgrade <release-name> sso-keycloak/sso-keycloak [--namespace <my-namespace>] [--version <x.y.z>] [--values ./custom-values.yaml]
```

### Uninstall this chart repository

```console
$ helm uninstall <release-name> [--namespace <my-namespace>]
```

## Configuration

The following table lists the configurable parameters of the Keycloak chart and their default values.

| Parameter                          | Description                                                      | Default                                                                                    |
| ---------------------------------- | ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| `replicaCount`                     | Number of pods to create                                         | `1`                                                                                        |
| `image.repository`                 | container image repository                                       | `ghcr.io/bcgov/sso`                                                                        |
| `image.tag`                        | container image tag                                              | `dev`                                                                                      |
| `image.pullPolicy`                 | container image pull policy                                      | `Always`                                                                                   |
| `nameOverride`                     | override for the chart name                                      | `sso-keycloak`                                                                             |
| `fullNameOverride`                 | override for the full chart name                                 | `sso-keycloak`                                                                             |
| `service.type`                     | type of service to create                                        | `ClusterIP`                                                                                |
| `service.port`                     | port of service                                                  | `8080`                                                                                     |
| `pingService.enabled`              | enable DNS ping                                                  | `true`                                                                                     |
| `pingService.port`                 | exposed port of ping service                                     | `8888`                                                                                     |
| `admin.username`                   | username of the Keycloak admin user                              |                                                                                            |
| `admin.password`                   | password of the Keycloak admin user                              |                                                                                            |
| `postgres.host`                    | host of postgres service                                         | `sso-pgsql-master`                                                                         |
| `postgres.database`                | name of database                                                 | `rhsso`                                                                                    |
| `postgres.port`                    | exposed port of database                                         | `5432`                                                                                     |
| `postgres.credentials.secret`      | name of secret containing database credentials                   | `sso-pgsql`                                                                                |
| `postgres.credentials.username`    | name of admin database user                                      | `postgres`                                                                                 |
| `postgres.credentials.passwordKey` | Secret key of admin password                                     | `password-superuser`                                                                       |
| `postgres.poolSize.min`            | Minimum pool size                                                | `5`                                                                                        |
| `postgres.poolSize.max`            | Maximum pool size                                                | `20`                                                                                       |
| `additionalServerOptions`          | Additional command line options for server                       | `-Dkeycloak.profile.feature.authorization=enabled -Djboss.persistent.log.dir=/var/log/eap` |
| `tls.enabled`                      | Enable tls                                                       | `false`                                                                                    |
| `persistentLog.enabled`            | Enable persistent logs                                           | `true`                                                                                     |
| `persistentLog.storageClassName`   | Storage class name of volume                                     | `netapp-file-standard`                                                                     |
| `persistentLog.path`               | Path to save logs                                                | `/var/log/eap`                                                                             |
| `resources.limits.memory`          | memory limit for pods                                            | `2Gi`                                                                                      |
| `resources.limits.cpu`             | CPU limit for pods                                               | `2`                                                                                        |
| `resources.requests.cpu`           | cpu request for pods                                             | `1250m`                                                                                    |
| `resources.requests.memory`        | memory request for pods                                          | `1Gi`                                                                                      |
| `nodeSelector`                     | node labels for pod assignment                                   | `{}`                                                                                       |
| `tolerations`                      | toleration settings                                              | `[]`                                                                                       |
| `affinity`                         | affinity settings                                                | `{}`                                                                                       |
| `affinityTemplate`                 | a template string to use to generate the affinity settings       |                                                                                            |
| `podDisruptionBudget.enabled`                 | does keycloak have a pod disruption budget | false  |
| `podDisruptionBudget.minAvailable`            | min number of keycloak pods available      |        |
| `podDisruptionBudget.maxUnavailable`          | max number of keycloak pods unavailable    |        |
| `maintenancePage.enabled`          | deploy maintenance page app                                      | `false`                                                                                    |
| `maintenancePage.active`           | forward incoming traffic to maintenance page app                 | `false`                                                                                    |
| `maintenancePage.replicaCount`     | number of maintenance app pods to create                         | `1`                                                                                        |
| `maintenancePage.image.repository` | maintenance app container image repository                       | `ghcr.io/bcgov/sso-maintenance`                                                            |
| `maintenancePage.image.tag`        | maintenance app container image tag                              | `dev`                                                                                      |
| `maintenancePage.image.pullPolicy` | maintenance app container image pull policy                      | `Always`                                                                                   |
| `configuration.enabled`            | create a ConfigMap for SSO configuration                         | `false`                                                                                    |
| `configuration.version`            | default configuration version when `configuration.data` is empty | `7.6`                                                                                      |
| `configuration.data`               | user-defined configuration value                                 |                                                                                            |
| `livenessProbe.enabled`            | enable livenessProbe                                             | `true`                                                                                     |
| `livenessProbe.verification`       | livenessProbe option; `script \| http`                           | `script`                                                                                   |
| `readinessProbe.enabled`           | enable readinessProbe settings                                   | `true`                                                                                     |
| `readinessProbe.verification`      | readinessProbe option; `script \| http`                          | `script`                                                                                   |
| `rollingUpdate`                    | rolling update process settings                                  | `{}`                                                                                       |
| `annotations.timeout`                | route timeout                                  | ""

### Notes

- The helm chart installs two `Secret` k8s objects:

  1. `<release-name>-admin`: it stores the Keycloak admin password.
  2. `<release-name>-jgroups`: it stores the Keycloak cluster jgroups password.

- It is recommended to use `--set-file` option when defining the user-defined configuration.

  ```yaml
  configuration:
    enabled: true
  ```

  ```console
  $ helm upgrade <release-name> sso-keycloak/sso-keycloak [--namespace <my-namespace>] [--version <x.y.z>] [--values ./custom-values.yaml] --set-file configuration.data=<xml-file-path>
  ```

  - see https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.2/html/getting_started_with_jboss_eap_for_openshift_online/migrating-application-openshift-4

- k8s resource object label conventions
  1. see https://kubernetes.io/docs/concepts/overview/working-with-objects/common-labels/#labels
