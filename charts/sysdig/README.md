# Sysdig Helm Chart

This directory contains a Kubernetes chart to deploy a BCGov Openshift `SysdigTeams Operator`.

## Prerequisites Details

- Kubernetes
- OpenShift Client

## Usages

### Add this chart repository

```console
$ helm repo add sso-charts https://bcgov.github.io/sso-helm-charts
```

### Install this chart repository

```console
$ helm install <release-name> sso-charts/sysdig [--version <x.y.z>] [--values ./custom-values.yaml]
```

### Upgrade this chart repository

```console
$ helm upgrade <release-name> sso-charts/sysdig [--version <x.y.z>] [--values ./custom-values.yaml]
```

### Uninstall this chart repository

```console
$ helm uninstall <release-name> [--namespace <my-namespace>]
```

## Configuration

The following table lists the configurable parameters of the sysdig chart.

| Parameter          | Description                        | Required | Default |
| ------------------ | ---------------------------------- | -------- | ------- |
| `nameOverride`     | Override the name of the chart     | `false`  | `nil`   |
| `fullnameOverride` | Override the fullname of the chart | `false`  | `nil`   |
| `licensePlate`     | The license plate of OCP project   | `true`   | `nil`   |
| `users`            | The team member list               | `true`   | `{}`    |

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

## Example Values file

```yaml
licensePlate: 3d5c3f
users:
  - name: sso.manager@gov.bc.ca
    role: ROLE_TEAM_MANAGER
  - name: sso.editor@gov.bc.ca
    role: ROLE_TEAM_EDIT
  - name: sso.standard@gov.bc.ca
    role: ROLE_TEAM_STANDARD
  - name: sso.reader@gov.bc.ca
    role: ROLE_TEAM_READ
```

see [Available Roles](https://developer.gov.bc.ca/Set-up-a-team-in-Sysdig-Monitor) for more available roles.
