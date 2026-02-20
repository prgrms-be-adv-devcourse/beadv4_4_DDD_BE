{{/* 리소스 이름 */}}
{{- define "settlementApi.fullname" -}}
{{- printf "%s-%s" .Release.Name "settlement-api" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "settlementApi.labels" -}}
app.kubernetes.io/name: settlement-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "settlementApi.selectorLabels" -}}
app: {{ include "settlementApi.fullname" . }}
{{- end }}
