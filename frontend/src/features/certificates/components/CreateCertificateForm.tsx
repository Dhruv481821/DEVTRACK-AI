import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { useCreateCertificate } from '../api/useCertificates';
import {
  createCertificateSchema,
  type CreateCertificateFormValues,
} from '../schemas/certificateSchemas';

export function CreateCertificateForm() {
  const createMutation = useCreateCertificate();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateCertificateFormValues>({
    resolver: zodResolver(createCertificateSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(values, {
      onSuccess: () => reset(),
    });
  });

  return (
    <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div>
        <Label htmlFor="new-cert-name">Certificate name</Label>
        <Input
          id="new-cert-name"
          placeholder="e.g. AWS Certified Developer"
          {...register('name')}
        />
        <FieldError message={errors.name?.message} />
      </div>

      <div>
        <Label htmlFor="new-cert-org">Issuing organization</Label>
        <Input
          id="new-cert-org"
          placeholder="e.g. Amazon Web Services"
          {...register('issuingOrg')}
        />
        <FieldError message={errors.issuingOrg?.message} />
      </div>

      <div>
        <Label htmlFor="new-cert-date">Issue date</Label>
        <Input id="new-cert-date" type="date" {...register('issueDate')} />
        <FieldError message={errors.issueDate?.message} />
      </div>

      <div>
        <Label htmlFor="new-cert-url">Verification URL</Label>
        <Input
          id="new-cert-url"
          placeholder="https://..."
          {...register('verificationUrl')}
        />
        <FieldError message={errors.verificationUrl?.message} />
      </div>

      <div className="sm:col-span-2">
        <Button type="submit" loading={createMutation.isPending}>
          Add certificate
        </Button>

        {createMutation.isError && (
          <p className="mt-2 font-body text-sm text-danger">
            Couldn&apos;t add that certificate. Please try again.
          </p>
        )}
      </div>
    </form>
  );
}