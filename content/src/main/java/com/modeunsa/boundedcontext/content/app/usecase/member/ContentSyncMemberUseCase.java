package com.modeunsa.boundedcontext.content.app.usecase.member;

import com.modeunsa.boundedcontext.content.app.dto.member.ContentMemberDto;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.out.ContentMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentSyncMemberUseCase {

  private final ContentMemberRepository contentMemberRepository;

  public void syncContentMember(ContentMemberDto contentMemberDto) {
    ContentMember contentMember =
        ContentMember.create(
            contentMemberDto.getMemberId(),
            contentMemberDto.getEmail(),
            contentMemberDto.getAuthor());
    contentMemberRepository.save(contentMember);
  }
}
