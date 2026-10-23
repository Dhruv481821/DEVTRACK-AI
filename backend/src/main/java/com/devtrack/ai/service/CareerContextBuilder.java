package com.devtrack.ai.service;

import com.devtrack.certificates.dto.response.CertificateResponse;
import com.devtrack.certificates.service.CertificateService;
import com.devtrack.dsa.dto.response.DsaTrendsResponse;
import com.devtrack.dsa.service.DsaProblemService;
import com.devtrack.github.dto.response.GithubSummaryResponse;
import com.devtrack.github.service.GithubSummaryService;
import com.devtrack.jobtracker.dto.response.JobApplicationResponse;
import com.devtrack.jobtracker.service.JobApplicationService;
import com.devtrack.project.dto.response.ProjectResponse;
import com.devtrack.project.service.ProjectService;
import com.devtrack.resume.dto.response.ResumeResponse;
import com.devtrack.resume.dto.response.ResumeSectionResponse;
import com.devtrack.resume.service.ResumeSectionService;
import com.devtrack.resume.service.ResumeService;
import com.devtrack.studyplanner.dto.response.StreakResponse;
import com.devtrack.studyplanner.dto.response.StudyPlanResponse;
import com.devtrack.studyplanner.service.StudyPlanService;
import com.devtrack.studyplanner.service.StudyStreakService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Builds the CAREER_ASSISTANT data block from other modules' SERVICES only (the ai module is the
 * one module explicitly allowed to read across modules — 07_Backend_Architecture.md §1). Every
 * value comes from an owner-scoped "my data" service method, so the block can only ever contain the
 * calling user's own data.
 *
 * <p>Summaries, not raw dumps (09_AI_Architecture.md §5): counts, distributions and capped lists,
 * so prompt size stays bounded regardless of how much history a user has.
 */
@Component
public class CareerContextBuilder {

  static final int MAX_TAGS = 10;
  static final int MAX_LIST_ITEMS = 15;
  static final int MAX_RESUME_CHARS = 4000;
  static final int MAX_FIELD_CHARS = 200;

  /** The prompt data block plus the human-readable list of sources that actually had data. */
  public record CareerContext(String profileDataBlock, List<String> basedOn) {}

  private final DsaProblemService dsaProblemService;
  private final GithubSummaryService githubSummaryService;
  private final ResumeService resumeService;
  private final ResumeSectionService resumeSectionService;
  private final JobApplicationService jobApplicationService;
  private final StudyPlanService studyPlanService;
  private final StudyStreakService studyStreakService;
  private final CertificateService certificateService;
  private final ProjectService projectService;

  public CareerContextBuilder(
      DsaProblemService dsaProblemService,
      GithubSummaryService githubSummaryService,
      ResumeService resumeService,
      ResumeSectionService resumeSectionService,
      JobApplicationService jobApplicationService,
      StudyPlanService studyPlanService,
      StudyStreakService studyStreakService,
      CertificateService certificateService,
      ProjectService projectService) {
    this.dsaProblemService = dsaProblemService;
    this.githubSummaryService = githubSummaryService;
    this.resumeService = resumeService;
    this.resumeSectionService = resumeSectionService;
    this.jobApplicationService = jobApplicationService;
    this.studyPlanService = studyPlanService;
    this.studyStreakService = studyStreakService;
    this.certificateService = certificateService;
    this.projectService = projectService;
  }

  public CareerContext build(UUID userId) {
    List<String> basedOn = new ArrayList<>();
    StringBuilder block = new StringBuilder("<profile_data>\n");

    appendDsa(userId, block, basedOn);
    appendGithub(userId, block, basedOn);
    appendResume(userId, block, basedOn);
    appendJobApplications(userId, block, basedOn);
    appendStudy(userId, block, basedOn);
    appendCertificates(userId, block, basedOn);
    appendProjects(userId, block, basedOn);

    block.append("</profile_data>\n");
    return new CareerContext(block.toString(), List.copyOf(basedOn));
  }

  private void appendDsa(UUID userId, StringBuilder block, List<String> basedOn) {
    DsaTrendsResponse trends = dsaProblemService.getTrends(userId);
    block.append("<dsa_practice>\n");
    if (trends.totalProblems() == 0) {
      block.append("no data\n");
    } else {
      block.append("problems solved: ").append(trends.totalProblems()).append('\n');
      block
          .append("by difficulty: ")
          .append(formatCounts(new TreeMap<>(trends.difficultyDistribution())))
          .append('\n');
      block.append("most practiced topics: ").append(topTags(trends.tagFrequency())).append('\n');
      basedOn.add("DSA practice (" + trends.totalProblems() + " problems)");
    }
    block.append("</dsa_practice>\n");
  }

  private void appendGithub(UUID userId, StringBuilder block, List<String> basedOn) {
    Optional<GithubSummaryResponse> summary = githubSummaryService.summarize(userId);
    block.append("<github>\n");
    if (summary.isEmpty() || summary.get().repoCount() == 0) {
      block.append(summary.isEmpty() ? "not connected\n" : "connected, no repositories\n");
    } else {
      GithubSummaryResponse s = summary.get();
      block.append("repositories: ").append(s.repoCount()).append('\n');
      block.append("total stars: ").append(s.totalStars()).append('\n');
      block
          .append("top languages: ")
          .append(s.topLanguages().isEmpty() ? "unknown" : String.join(", ", s.topLanguages()))
          .append('\n');
      basedOn.add("GitHub (" + s.repoCount() + " repositories)");
    }
    block.append("</github>\n");
  }

  // Only the most recently updated resume — the one the user is actively working on.
  private void appendResume(UUID userId, StringBuilder block, List<String> basedOn) {
    Optional<ResumeResponse> latest =
        resumeService.listMyResumes(userId).stream()
            .max(Comparator.comparing(ResumeResponse::updatedAt));
    if (latest.isEmpty()) {
      block.append("<resume>\nno data\n</resume>\n");
      return;
    }
    ResumeResponse resume = latest.get();
    List<ResumeSectionResponse> sections =
        resumeSectionService.listSectionsForResume(resume.id(), userId);

    StringBuilder body = new StringBuilder();
    sections.stream()
        .sorted(Comparator.comparingInt(ResumeSectionResponse::orderIndex))
        .forEach(
            section -> {
              body.append("[").append(sanitize(section.sectionType())).append("]\n");
              new TreeMap<>(section.content())
                  .forEach(
                      (key, value) ->
                          body.append(sanitize(key))
                              .append(": ")
                              .append(sanitize(String.valueOf(value)))
                              .append('\n'));
            });

    block
        .append("<resume title=\"")
        .append(sanitize(resume.title()).replace("\"", "'"))
        .append("\">\n")
        .append(
            body.isEmpty() ? "no sections" : truncate(body.toString(), MAX_RESUME_CHARS).strip())
        .append("\n</resume>\n");
    basedOn.add("Resume: " + truncate(resume.title(), 80));
  }

  private void appendJobApplications(UUID userId, StringBuilder block, List<String> basedOn) {
    List<JobApplicationResponse> applications = jobApplicationService.listMyApplications(userId);
    block.append("<job_applications>\n");
    if (applications.isEmpty()) {
      block.append("no data\n");
    } else {
      Map<String, Long> byStage =
          applications.stream()
              .collect(
                  Collectors.groupingBy(
                      a -> a.stage().name(), TreeMap::new, Collectors.counting()));
      block.append("total: ").append(applications.size()).append('\n');
      block.append("by stage: ").append(formatCounts(byStage)).append('\n');
      block.append("recent roles: ");
      block.append(
          applications.stream()
              .limit(MAX_LIST_ITEMS)
              .map(a -> sanitize(a.roleTitle()) + " at " + sanitize(a.companyName()))
              .collect(Collectors.joining("; ")));
      block.append('\n');
      basedOn.add("Job applications (" + applications.size() + ")");
    }
    block.append("</job_applications>\n");
  }

  private void appendStudy(UUID userId, StringBuilder block, List<String> basedOn) {
    List<StudyPlanResponse> plans = studyPlanService.getPlans(userId);
    StreakResponse streak = studyStreakService.getStreak(userId);
    block.append("<study>\n");
    if (plans.isEmpty() && streak.lastActivityDate() == null) {
      block.append("no data\n");
    } else {
      block
          .append("current streak (days): ")
          .append(streak.currentStreak())
          .append(", longest: ")
          .append(streak.longestStreak())
          .append('\n');
      plans.stream()
          .limit(MAX_LIST_ITEMS)
          .forEach(
              p ->
                  block
                      .append("plan: ")
                      .append(sanitize(p.title()))
                      .append(" (")
                      .append(p.completedTasks())
                      .append('/')
                      .append(p.totalTasks())
                      .append(" tasks done")
                      .append(p.targetDate() != null ? ", target " + p.targetDate() : "")
                      .append(")\n"));
      basedOn.add("Study plans and streak");
    }
    block.append("</study>\n");
  }

  private void appendCertificates(UUID userId, StringBuilder block, List<String> basedOn) {
    List<CertificateResponse> certificates = certificateService.listMyCertificates(userId);
    block.append("<certificates>\n");
    if (certificates.isEmpty()) {
      block.append("no data\n");
    } else {
      certificates.stream()
          .limit(MAX_LIST_ITEMS)
          .forEach(
              c ->
                  block
                      .append(sanitize(c.name()))
                      .append(" — ")
                      .append(sanitize(c.issuingOrg()))
                      .append('\n'));
      basedOn.add("Certificates (" + certificates.size() + ")");
    }
    block.append("</certificates>\n");
  }

  private void appendProjects(UUID userId, StringBuilder block, List<String> basedOn) {
    List<ProjectResponse> projects = projectService.getProjects(userId);
    block.append("<projects>\n");
    if (projects.isEmpty()) {
      block.append("no data\n");
    } else {
      projects.stream()
          .limit(MAX_LIST_ITEMS)
          .forEach(
              p -> {
                block.append(sanitize(p.name()));
                if (p.description() != null && !p.description().isBlank()) {
                  block.append(": ").append(sanitize(p.description()));
                }
                block.append('\n');
              });
      basedOn.add("Projects (" + projects.size() + ")");
    }
    block.append("</projects>\n");
  }

  private String topTags(Map<String, Long> tagFrequency) {
    if (tagFrequency.isEmpty()) {
      return "none tagged";
    }
    return tagFrequency.entrySet().stream()
        .sorted(
            Map.Entry.<String, Long>comparingByValue()
                .reversed()
                .thenComparing(Map.Entry.comparingByKey()))
        .limit(MAX_TAGS)
        .map(e -> sanitize(e.getKey()) + " (" + e.getValue() + ")")
        .collect(Collectors.joining(", "));
  }

  private static String formatCounts(Map<String, Long> counts) {
    return counts.entrySet().stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining(", "));
  }

  /**
   * User-authored text must not be able to close or open the delimiter tags the system instruction
   * relies on (09_AI_Architecture.md §4), so angle brackets are neutralised and each field is
   * length-capped.
   */
  static String sanitize(String value) {
    if (value == null) {
      return "";
    }
    return truncate(value.replace("<", "‹").replace(">", "›"), MAX_FIELD_CHARS);
  }

  /** The question gets the same tag-neutralising as data fields, with the request's own cap. */
  static String sanitizeQuestion(String question) {
    return question.replace("<", "‹").replace(">", "›");
  }

  static String truncate(String value, int max) {
    if (value == null) {
      return "";
    }
    return value.length() <= max ? value : value.substring(0, max) + "…";
  }
}
