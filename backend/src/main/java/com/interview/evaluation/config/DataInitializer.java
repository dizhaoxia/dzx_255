package com.interview.evaluation.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.interview.evaluation.entity.*;
import com.interview.evaluation.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private EvaluationDimensionMapper evaluationDimensionMapper;

    @Autowired
    private InterviewTaskMapper interviewTaskMapper;

    @Autowired
    private BehaviorTagMapper behaviorTagMapper;

    @Autowired
    private EvaluationTemplateMapper evaluationTemplateMapper;

    @Autowired
    private TemplateDimensionMapper templateDimensionMapper;

    @Autowired
    private PositionTemplateMapper positionTemplateMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initDimensions();
        initUsers();
        initPositions();
        initCandidates();
        initInterviewTasks();
        initBehaviorTags();
        initEvaluationTemplate();
    }

    private void initDimensions() {
        Long count = evaluationDimensionMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        String[][] dimensions = {
            {"PROFESSIONAL_SKILL", "专业技能", "考察候选人的专业知识和技术能力", "1"},
            {"COMMUNICATION", "沟通表达", "考察候选人的语言表达和沟通能力", "2"},
            {"TEAMWORK", "团队协作", "考察候选人的团队合作精神和协作能力", "3"},
            {"JOB_FIT", "岗位匹配度", "考察候选人与岗位要求的匹配程度", "4"},
            {"LEARNING_ABILITY", "学习能力", "考察候选人的学习能力和发展潜力", "5"}
        };

        for (String[] dim : dimensions) {
            EvaluationDimension dimension = new EvaluationDimension();
            dimension.setDimensionCode(dim[0]);
            dimension.setDimensionName(dim[1]);
            dimension.setDimensionDesc(dim[2]);
            dimension.setMaxScore(5);
            dimension.setMinScore(1);
            dimension.setSortOrder(Integer.parseInt(dim[3]));
            dimension.setIsDefault(1);
            evaluationDimensionMapper.insert(dimension);
        }

        System.out.println("====== 评分维度数据初始化完成 ======");
    }

    private void initUsers() {
        Long count = sysUserMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        String encodedPassword = passwordEncoder.encode("123456");

        SysUser hr = new SysUser();
        hr.setUsername("hr001");
        hr.setPassword(encodedPassword);
        hr.setRealName("张HR");
        hr.setRole("HR");
        hr.setEmail("hr001@example.com");
        hr.setPhone("13800000001");
        hr.setStatus(1);
        sysUserMapper.insert(hr);

        SysUser interviewer1 = new SysUser();
        interviewer1.setUsername("interviewer001");
        interviewer1.setPassword(encodedPassword);
        interviewer1.setRealName("李技术");
        interviewer1.setRole("INTERVIEWER");
        interviewer1.setEmail("lijs@example.com");
        interviewer1.setPhone("13800000002");
        interviewer1.setStatus(1);
        sysUserMapper.insert(interviewer1);

        SysUser interviewer2 = new SysUser();
        interviewer2.setUsername("interviewer002");
        interviewer2.setPassword(encodedPassword);
        interviewer2.setRealName("王技术");
        interviewer2.setRole("INTERVIEWER");
        interviewer2.setEmail("wangjs@example.com");
        interviewer2.setPhone("13800000003");
        interviewer2.setStatus(1);
        sysUserMapper.insert(interviewer2);

        System.out.println("====== 用户数据初始化完成 ======");
        System.out.println("HR账号: hr001 / 123456");
        System.out.println("面试官账号: interviewer001 / 123456");
        System.out.println("面试官账号: interviewer002 / 123456");
    }

    private void initPositions() {
        Long count = positionMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        Position p1 = new Position();
        p1.setPositionName("高级Java开发工程师");
        p1.setPositionDesc("负责公司核心业务系统的设计与开发");
        p1.setDepartment("技术部");
        p1.setStatus(1);
        positionMapper.insert(p1);

        Position p2 = new Position();
        p2.setPositionName("前端开发工程师");
        p2.setPositionDesc("负责公司Web端产品的前端开发");
        p2.setDepartment("技术部");
        p2.setStatus(1);
        positionMapper.insert(p2);

        Position p3 = new Position();
        p3.setPositionName("产品经理");
        p3.setPositionDesc("负责产品规划和需求分析");
        p3.setDepartment("产品部");
        p3.setStatus(1);
        positionMapper.insert(p3);

        System.out.println("====== 岗位数据初始化完成 ======");
    }

    private void initCandidates() {
        Long count = candidateMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        List<Position> positions = positionMapper.selectList(null);
        Long javaPosId = positions.stream()
                .filter(p -> p.getPositionName().contains("Java"))
                .findFirst()
                .map(Position::getId)
                .orElse(1L);
        Long fePosId = positions.stream()
                .filter(p -> p.getPositionName().contains("前端"))
                .findFirst()
                .map(Position::getId)
                .orElse(2L);

        Candidate c1 = new Candidate();
        c1.setName("张三");
        c1.setGender("男");
        c1.setAge(28);
        c1.setPhone("13900000001");
        c1.setEmail("zhangsan@example.com");
        c1.setPositionId(javaPosId);
        c1.setEducation("本科");
        c1.setWorkYears(5);
        c1.setStatus("TECH_INTERVIEW");
        candidateMapper.insert(c1);

        Candidate c2 = new Candidate();
        c2.setName("李四");
        c2.setGender("女");
        c2.setAge(25);
        c2.setPhone("13900000002");
        c2.setEmail("lisi@example.com");
        c2.setPositionId(javaPosId);
        c2.setEducation("硕士");
        c2.setWorkYears(3);
        c2.setStatus("TECH_INTERVIEW");
        candidateMapper.insert(c2);

        Candidate c3 = new Candidate();
        c3.setName("王五");
        c3.setGender("男");
        c3.setAge(30);
        c3.setPhone("13900000003");
        c3.setEmail("wangwu@example.com");
        c3.setPositionId(fePosId);
        c3.setEducation("本科");
        c3.setWorkYears(7);
        c3.setStatus("PENDING");
        candidateMapper.insert(c3);

        System.out.println("====== 候选人数据初始化完成 ======");
    }

    private void initInterviewTasks() {
        Long count = interviewTaskMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        List<Candidate> candidates = candidateMapper.selectList(null);
        List<SysUser> interviewers = sysUserMapper.selectList(
                new QueryWrapper<SysUser>().eq("role", "INTERVIEWER")
        );

        if (candidates.isEmpty() || interviewers.isEmpty()) {
            return;
        }

        Candidate c1 = candidates.get(0);
        Candidate c2 = candidates.get(1);
        Candidate c3 = candidates.get(2);
        SysUser i1 = interviewers.get(0);
        SysUser i2 = interviewers.get(1);

        InterviewTask t1 = new InterviewTask();
        t1.setCandidateId(c1.getId());
        t1.setPositionId(c1.getPositionId());
        t1.setInterviewerId(i1.getId());
        t1.setInterviewRound(1);
        t1.setInterviewType("技术面");
        t1.setInterviewTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        t1.setVideoUrl("https://video.example.com/play/room101");
        t1.setStatus("PENDING");
        interviewTaskMapper.insert(t1);

        InterviewTask t2 = new InterviewTask();
        t2.setCandidateId(c1.getId());
        t2.setPositionId(c1.getPositionId());
        t2.setInterviewerId(i2.getId());
        t2.setInterviewRound(2);
        t2.setInterviewType("技术面");
        t2.setInterviewTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
        t2.setVideoUrl("https://video.example.com/play/room102");
        t2.setStatus("PENDING");
        interviewTaskMapper.insert(t2);

        InterviewTask t3 = new InterviewTask();
        t3.setCandidateId(c2.getId());
        t3.setPositionId(c2.getPositionId());
        t3.setInterviewerId(i1.getId());
        t3.setInterviewRound(1);
        t3.setInterviewType("技术面");
        t3.setInterviewTime(LocalDateTime.now().plusDays(3).withHour(9).withMinute(30));
        t3.setVideoUrl("https://video.example.com/play/room103");
        t3.setStatus("PENDING");
        interviewTaskMapper.insert(t3);

        InterviewTask t4 = new InterviewTask();
        t4.setCandidateId(c3.getId());
        t4.setPositionId(c3.getPositionId());
        t4.setInterviewerId(i1.getId());
        t4.setInterviewRound(1);
        t4.setInterviewType("技术面");
        t4.setInterviewTime(LocalDateTime.now().plusDays(4).withHour(11).withMinute(0));
        t4.setVideoUrl("https://video.example.com/play/room104");
        t4.setStatus("PENDING");
        interviewTaskMapper.insert(t4);

        System.out.println("====== 面试任务数据初始化完成 ======");
    }

    private void initBehaviorTags() {
        Long count = behaviorTagMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        List<EvaluationDimension> dimensions = evaluationDimensionMapper.selectList(null);

        List<String[]> professionalTags = Arrays.asList(
            new String[]{"技术扎实", "POSITIVE"},
            new String[]{"经验丰富", "POSITIVE"},
            new String[]{"学习能力强", "POSITIVE"},
            new String[]{"逻辑清晰", "POSITIVE"},
            new String[]{"基础薄弱", "NEGATIVE"},
            new String[]{"经验不足", "NEGATIVE"},
            new String[]{"代码规范差", "NEGATIVE"}
        );
        List<String[]> communicationTags = Arrays.asList(
            new String[]{"表达流畅", "POSITIVE"},
            new String[]{"条理清晰", "POSITIVE"},
            new String[]{"善于沟通", "POSITIVE"},
            new String[]{"表达不清", "NEGATIVE"},
            new String[]{"缺乏准备", "NEGATIVE"},
            new String[]{"答非所问", "NEGATIVE"}
        );
        List<String[]> teamworkTags = Arrays.asList(
            new String[]{"团队意识强", "POSITIVE"},
            new String[]{"协作能力好", "POSITIVE"},
            new String[]{"积极主动", "POSITIVE"},
            new String[]{"独断专行", "NEGATIVE"},
            new String[]{"缺乏协作", "NEGATIVE"}
        );
        List<String[]> jobFitTags = Arrays.asList(
            new String[]{"高度匹配", "POSITIVE"},
            new String[]{"有相关经验", "POSITIVE"},
            new String[]{"潜力大", "POSITIVE"},
            new String[]{"经验不匹配", "NEGATIVE"},
            new String[]{"期望不符", "NEGATIVE"}
        );
        List<String[]> learningTags = Arrays.asList(
            new String[]{"学习能力强", "POSITIVE"},
            new String[]{"自驱力强", "POSITIVE"},
            new String[]{"求知欲强", "POSITIVE"},
            new String[]{"学习被动", "NEGATIVE"},
            new String[]{"缺乏思考", "NEGATIVE"}
        );

        int sortOrder = 0;
        for (EvaluationDimension dim : dimensions) {
            List<String[]> tags;
            switch (dim.getDimensionCode()) {
                case "PROFESSIONAL_SKILL":
                    tags = professionalTags;
                    break;
                case "COMMUNICATION":
                    tags = communicationTags;
                    break;
                case "TEAMWORK":
                    tags = teamworkTags;
                    break;
                case "JOB_FIT":
                    tags = jobFitTags;
                    break;
                case "LEARNING_ABILITY":
                    tags = learningTags;
                    break;
                default:
                    tags = new ArrayList<>();
            }
            sortOrder = 0;
            for (String[] tag : tags) {
                BehaviorTag behaviorTag = new BehaviorTag();
                behaviorTag.setDimensionId(dim.getId());
                behaviorTag.setTagName(tag[0]);
                behaviorTag.setTagType(tag[1]);
                behaviorTag.setSortOrder(sortOrder++);
                behaviorTagMapper.insert(behaviorTag);
            }
        }

        System.out.println("====== 行为标签数据初始化完成 ======");
    }

    private void initEvaluationTemplate() {
        Long count = evaluationTemplateMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        EvaluationTemplate template = new EvaluationTemplate();
        template.setTemplateName("通用技术岗位评价模板");
        template.setTemplateDesc("适用于各类技术岗位的通用评价模板，包含5个核心维度");
        template.setIsDefault(1);
        template.setStatus(1);
        evaluationTemplateMapper.insert(template);

        List<EvaluationDimension> dimensions = evaluationDimensionMapper.selectList(
            new QueryWrapper<EvaluationDimension>().eq("is_default", 1).orderByAsc("sort_order")
        );

        int[] weights = {30, 20, 15, 20, 15};
        for (int i = 0; i < dimensions.size(); i++) {
            EvaluationDimension dim = dimensions.get(i);
            TemplateDimension td = new TemplateDimension();
            td.setTemplateId(template.getId());
            td.setDimensionId(dim.getId());
            td.setDimensionCode(dim.getDimensionCode());
            td.setDimensionName(dim.getDimensionName());
            td.setDimensionDesc(dim.getDimensionDesc());
            td.setWeightPercent(weights[i]);
            td.setMaxScore(dim.getMaxScore());
            td.setMinScore(dim.getMinScore());
            td.setSortOrder(i + 1);
            templateDimensionMapper.insert(td);
        }

        List<Position> positions = positionMapper.selectList(null);
        for (Position pos : positions) {
            PositionTemplate pt = new PositionTemplate();
            pt.setPositionId(pos.getId());
            pt.setTemplateId(template.getId());
            positionTemplateMapper.insert(pt);
        }

        System.out.println("====== 评价模板数据初始化完成 ======");
    }
}
