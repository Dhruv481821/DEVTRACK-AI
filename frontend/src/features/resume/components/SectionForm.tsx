import { ExperienceSectionForm } from './sectionForms/ExperienceSectionForm';
import { EducationSectionForm } from './sectionForms/EducationSectionForm';
import { ProjectsSectionForm } from './sectionForms/ProjectsSectionForm';
import { SkillsSectionForm } from './sectionForms/SkillsSectionForm';
import type { SectionType } from '../schemas/sectionSchemas';

interface SectionFormProps {
  sectionType: SectionType;
  initialContent?: Record<string, unknown>;
  onSubmit: (content: Record<string, unknown>) => void;
  onCancel: () => void;
  submitting?: boolean;
}

// Dispatches to the right per-type form — see sectionSchemas.ts for why the field
// sets differ per type. Each concrete form owns its own strongly-typed useForm
// instance rather than sharing one generically-typed form across four disjoint
// field shapes, which would need unsafe casts to work at all.
export function SectionForm(props: SectionFormProps) {
  switch (props.sectionType) {
    case 'EXPERIENCE':
      return <ExperienceSectionForm {...props} />;
    case 'EDUCATION':
      return <EducationSectionForm {...props} />;
    case 'PROJECTS':
      return <ProjectsSectionForm {...props} />;
    case 'SKILLS':
      return <SkillsSectionForm {...props} />;
  }
}