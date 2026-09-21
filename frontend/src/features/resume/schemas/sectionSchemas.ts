import { z } from 'zod';

export const SECTION_TYPES = ['EXPERIENCE', 'EDUCATION', 'PROJECTS', 'SKILLS'] as const;
export type SectionType = (typeof SECTION_TYPES)[number];

export const SECTION_TYPE_LABELS: Record<SectionType, string> = {
  EXPERIENCE: 'Experience',
  EDUCATION: 'Education',
  PROJECTS: 'Projects',
  SKILLS: 'Skills',
};

// resume_section.content has no schema anywhere in the docs — it's schemaless jsonb
// on the backend by design (V9__resume_builder.sql). The field sets below are a v1
// product decision made here, chosen to satisfy FR-RESUME-01's "form-driven editor,
// not free-text" requirement — extending them later needs no backend migration, just
// a schema/form change on this side. Bulleted lists are edited as one line per item
// in a plain textarea rather than a dynamic add/remove-row field array, to keep the
// v1 form simple; that richer editing UX is a follow-up, not built here.

function splitLines(text: string | undefined): string[] {
  return (text ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);
}

// ---- Experience ----

export const experienceSectionSchema = z.object({
  company: z.string().min(1, 'Company is required').max(200),
  role: z.string().min(1, 'Role is required').max(200),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().optional(),
  bulletsText: z.string().optional(),
});
export type ExperienceSectionFormValues = z.infer<typeof experienceSectionSchema>;

export function experienceContentToForm(content: Record<string, unknown>): ExperienceSectionFormValues {
  return {
    company: typeof content.company === 'string' ? content.company : '',
    role: typeof content.role === 'string' ? content.role : '',
    startDate: typeof content.startDate === 'string' ? content.startDate : '',
    endDate: typeof content.endDate === 'string' ? content.endDate : '',
    bulletsText: Array.isArray(content.bullets) ? (content.bullets as string[]).join('\n') : '',
  };
}

export function experienceFormToContent(values: ExperienceSectionFormValues): Record<string, unknown> {
  return {
    company: values.company,
    role: values.role,
    startDate: values.startDate,
    endDate: values.endDate || null,
    bullets: splitLines(values.bulletsText),
  };
}

// ---- Education ----

export const educationSectionSchema = z.object({
  institution: z.string().min(1, 'Institution is required').max(200),
  degree: z.string().min(1, 'Degree is required').max(200),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().optional(),
});
export type EducationSectionFormValues = z.infer<typeof educationSectionSchema>;

export function educationContentToForm(content: Record<string, unknown>): EducationSectionFormValues {
  return {
    institution: typeof content.institution === 'string' ? content.institution : '',
    degree: typeof content.degree === 'string' ? content.degree : '',
    startDate: typeof content.startDate === 'string' ? content.startDate : '',
    endDate: typeof content.endDate === 'string' ? content.endDate : '',
  };
}

export function educationFormToContent(values: EducationSectionFormValues): Record<string, unknown> {
  return {
    institution: values.institution,
    degree: values.degree,
    startDate: values.startDate,
    endDate: values.endDate || null,
  };
}

// ---- Projects ----

export const projectsSectionSchema = z.object({
  name: z.string().min(1, 'Project name is required').max(200),
  description: z.string().max(1000).optional(),
  link: z.string().url('Enter a valid URL').optional().or(z.literal('')),
  bulletsText: z.string().optional(),
});
export type ProjectsSectionFormValues = z.infer<typeof projectsSectionSchema>;

export function projectsContentToForm(content: Record<string, unknown>): ProjectsSectionFormValues {
  return {
    name: typeof content.name === 'string' ? content.name : '',
    description: typeof content.description === 'string' ? content.description : '',
    link: typeof content.link === 'string' ? content.link : '',
    bulletsText: Array.isArray(content.bullets) ? (content.bullets as string[]).join('\n') : '',
  };
}

export function projectsFormToContent(values: ProjectsSectionFormValues): Record<string, unknown> {
  return {
    name: values.name,
    description: values.description || null,
    link: values.link || null,
    bullets: splitLines(values.bulletsText),
  };
}

// ---- Skills ----

export const skillsSectionSchema = z.object({
  skillsText: z.string().min(1, 'Enter at least one skill'),
});
export type SkillsSectionFormValues = z.infer<typeof skillsSectionSchema>;

export function skillsContentToForm(content: Record<string, unknown>): SkillsSectionFormValues {
  return {
    skillsText: Array.isArray(content.skills) ? (content.skills as string[]).join('\n') : '',
  };
}

export function skillsFormToContent(values: SkillsSectionFormValues): Record<string, unknown> {
  return {
    skills: splitLines(values.skillsText),
  };
}