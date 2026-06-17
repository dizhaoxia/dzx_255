package com.interview.evaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.evaluation.entity.Candidate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CandidateMapper extends BaseMapper<Candidate> {
}
