{{/* 리소스 이름 */}}
{{- define "frontend.fullname" -}}
{{- printf "%s-%s" .Release.Name "frontend" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "frontend.labels" -}}
app.kubernetes.io/name: frontend
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "frontend.selectorLabels" -}}
app: {{ include "frontend.fullname" . }}
{{- end }}