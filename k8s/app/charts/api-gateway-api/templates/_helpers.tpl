{{/* 리소스 이름 */}}
{{- define "apiGatewayApi.fullname" -}}
{{- printf "%s-%s" .Release.Name "api-gateway-api" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "apiGatewayApi.labels" -}}
app.kubernetes.io/name: api-gateway-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "apiGatewayApi.selectorLabels" -}}
app: {{ include "apiGatewayApi.fullname" . }}
{{- end }}
