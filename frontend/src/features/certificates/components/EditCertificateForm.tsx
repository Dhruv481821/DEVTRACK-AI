import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import type { CertificateResponse } from '../api/useCertificates';
import { useUpdateCertificate } from '../api/useCertificates';
import {
  updateCertificateSchema,
  type UpdateCertificateFormValues,
} from '../schemas/certificateSchemas';

interface EditCertificateFormProps {
  certificate: CertificateResponse;
  onDone: () => void;
}

export function EditCertificateForm({
  certificate,
  onDone,
}: EditCertificateFormProps) {
  const updateMutation = useUpdateCertificate(certificate.id);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateCertificateFormValues>({
    resolver: zodResolver(updateCertificateSchema),
    defaultValues: {
      name: certificate.name,
      issuingOrg: certificate.issuingOrg,
      issueDate: certificate.issueDate ?? '',
      verificationUrl: certificate.verificationUrl ?? '',
    },
  });

  const onSubmit = handleSubmit((values) => {
    updateMutation.mutate(values, {
      onSuccess: onDone,
    });
  });

  return (
    <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div>
        <Label htmlFor={`edit-cert-name-${certificate.id}`}>
          Certificate name
        </Label>
        <Input
          id={`edit-cert-name-${certificate.id}`}
          {...register('name')}
        />
        <FieldError message={errors.name?.message} />
      </div>

      <div>
        <Label htmlFor={`edit-cert-org-${certificate.id}`}>
          Issuing organization
        </Label>
        <Input
          id={`edit-cert-org-${certificate.id}`}
          {...register('issuingOrg')}
        />
        <FieldError message={errors.issuingOrg?.message} />
      </div>

      <div>
        <Label htmlFor={`edit-cert-date-${certificate.id}`}>
          Issue date
        </Label>
        <Input
          id={`edit-cert-date-${certificate.id}`}
          type="date"
          {...register('issueDate')}
        />
        <FieldError message={errors.issueDate?.message} />
      </div>

      <div>
        <Label htmlFor={`edit-cert-url-${certificate.id}`}>
          Verification URL
        </Label>
        <Input
          id={`edit-cert-url-${certificate.id}`}
          {...register('verificationUrl')}
        />
        <FieldError message={errors.verificationUrl?.message} />
      </div>

      <div className="flex items-center gap-3 sm:col-span-2">
        <Button type="submit" loading={updateMutation.isPending}>
          Save
        </Button>

        <button
          type="button"
          onClick={onDone}
          className="font-body text-sm text-text-muted hover:text-text-primary"
        >
          Cancel
        </button>

        {updateMutation.isError && (
          <p className="font-body text-sm text-danger">
            Couldn&apos;t save. Please try again.
          </p>
        )}
      </div>
    </form>
  );
}