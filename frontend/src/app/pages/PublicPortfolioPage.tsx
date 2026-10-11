import { useParams } from 'react-router-dom';
import { PublicPortfolio } from '@/features/portfolio/components/PublicPortfolio';

// Public route: rendered outside RequireAuth/AppShell.
export function PublicPortfolioPage() {
  const { slug } = useParams<{ slug: string }>();

  return (
    <main className="mx-auto min-h-screen max-w-2xl px-4 py-12">
      {slug ? <PublicPortfolio slug={slug} /> : null}
    </main>
  );
}