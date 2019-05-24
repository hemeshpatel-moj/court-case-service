package uk.gov.justice.digital.probation.court.list.courtlistservice.transformer;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.api.Block;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity.BlockType;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity.BlocksType;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BlockTransformer {
    private final CaseTransformer caseTransformer;

    public BlockTransformer(CaseTransformer caseTransformer) {
        this.caseTransformer = caseTransformer;
    }

    private Block toBlock(BlockType block) {
        return Block
                .builder()
                .id(block.getSbId())
                .description(block.getDesc())
                .startTime(DateTimeHelper.asTime(block.getBstart()))
                .endTime(DateTimeHelper.asTime(block.getBend()))
                .cases(caseTransformer.toCases(block.getCases()))
                .build();
    }

    public List<Block> toBlocks(BlocksType blocks) {
        return blocks.getBlock().stream().map(this::toBlock).collect(Collectors.toList());
    }
}
