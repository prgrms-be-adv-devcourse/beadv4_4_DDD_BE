package com.modeunsa.shared.member.dto.request;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(@NotNull MemberRole role) {}
