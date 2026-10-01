import { CertificateList } from '@/features/certificates/components/CertificateList';

export function CertificatesPage() {
  return (
    <div>
      <h1 className="mb-6 font-display text-2xl">Certificates</h1>
      <CertificateList />
    </div>
  );
}