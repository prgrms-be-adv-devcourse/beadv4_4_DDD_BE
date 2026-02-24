{{/* 리소스 이름 */}}
{{- define "inventoryApi.fullname" -}}
{{- printf "%s-%s" .Release.Name "inventory-api" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "inventoryApi.labels" -}}
app.kubernetes.io/name: inventory-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "inventoryApi.selectorLabels" -}}
app: {{ include "inventoryApi.fullname" . }}
{{- end }}
