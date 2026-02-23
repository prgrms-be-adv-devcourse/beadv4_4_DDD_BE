{{/* 리소스 이름 */}}
{{- define "paymentApi.fullname" -}}
{{- printf "%s-%s" .Release.Name "payment-api" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "paymentApi.labels" -}}
app.kubernetes.io/name: payment-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "paymentApi.selectorLabels" -}}
app: {{ include "paymentApi.fullname" . }}
{{- end }}
