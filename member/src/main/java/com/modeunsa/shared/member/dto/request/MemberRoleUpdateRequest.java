package com.modeunsa.shared.member.dto.request;

import com.modeunsa.shared.member.MemberRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(@NotNull MemberRole role) {}
