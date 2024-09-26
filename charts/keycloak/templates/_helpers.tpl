{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the project.
*/}}
{{- define "sso-keycloak.project" -}}
{{- default .Chart.Name .Values.project | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the name of the chart.
*/}}
{{- define "sso-keycloak.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "sso-keycloak.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "sso-keycloak.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Keycloak Common labels
*/}}
{{- define "sso-keycloak.labels" -}}
{{ include "sso-keycloak.commonlabels" . }}
{{ include "sso-keycloak.selectorLabels" . }}
{{- end }}

{{/*
Maintenance Common labels
*/}}
{{- define "sso-keycloak.maintenanceLabels" -}}
{{ include "sso-keycloak.commonlabels" . }}
{{ include "sso-keycloak.maintenanceSelectorLabels" . }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "sso-keycloak.commonlabels" -}}
helm.sh/chart: {{ include "sso-keycloak.chart" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: {{ include "sso-keycloak.project" . }}
{{- end }}

{{/*
Keycloak Selector labels
*/}}
{{- define "sso-keycloak.selectorLabels" -}}
app.kubernetes.io/name: {{ include "sso-keycloak.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Maintenance Selector labels
*/}}
{{- define "sso-keycloak.maintenanceSelectorLabels" -}}
app.kubernetes.io/name: {{ include "sso-keycloak.name" . }}-maintenance
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "sso-keycloak.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "sso-keycloak.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}
