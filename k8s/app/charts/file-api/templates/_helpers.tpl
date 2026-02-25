{{/* 리소스 이름 */}}
{{- define "fileApi.fullname" -}}
{{- printf "%s-%s" .Release.Name "file-api" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "fileApi.labels" -}}
app.kubernetes.io/name: file-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "fileApi.selectorLabels" -}}
app: {{ include "fileApi.fullname" . }}
{{- end }}
