{{/* 리소스 이름 */}}
{{- define "memberApi.fullname" -}}
{{- printf "%s-%s" .Release.Name "member-api" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "memberApi.labels" -}}
app.kubernetes.io/name: member-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "memberApi.selectorLabels" -}}
app: {{ include "memberApi.fullname" . }}
{{- end }}
