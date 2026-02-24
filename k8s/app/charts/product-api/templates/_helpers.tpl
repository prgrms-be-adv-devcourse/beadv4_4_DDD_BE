{{/* 리소스 이름 */}}
{{- define "productApi.fullname" -}}
{{- printf "%s-%s" .Release.Name "product-api" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "productApi.labels" -}}
app.kubernetes.io/name: product-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "productApi.selectorLabels" -}}
app: {{ include "productApi.fullname" . }}
{{- end }}
