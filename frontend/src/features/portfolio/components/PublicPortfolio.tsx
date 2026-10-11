import { ApiError } from '@/shared/api-client/types';
import { usePublicPortfolio } from '../api/usePortfolio';

function safeHttpUrl(url: string | null): string | null {
  if (!url) return null;

  return /^https?:\/\//i.test(url.trim())
    ? url.trim()
    : null;
}

export function PublicPortfolio({ slug }: { slug: string }) {
  const { data, isLoading, error } = usePublicPortfolio(slug);

  if (isLoading) {
    return (
      <p className="font-body text-sm text-text-muted">
        Loading portfolio…
      </p>
    );
  }

  if (error) {
    const notFound =
      error instanceof ApiError && error.status === 404;

    return (
      <div>
        <h1 className="font-display text-2xl">
          {notFound
            ? 'Portfolio not found'
            : 'Something went wrong'}
        </h1>

        <p className="mt-2 font-body text-sm text-text-muted">
          {notFound
            ? "This portfolio doesn't exist or hasn't been published."
            : "We couldn't load this portfolio. Please try again."}
        </p>
      </div>
    );
  }

  if (!data) return null;

  const avatar = safeHttpUrl(data.avatarUrl);

  return (
    <article className="space-y-10">
      <header className="flex items-center gap-4">
        {avatar && (
          <img
            src={avatar}
            alt=""
            className="h-16 w-16 rounded-full border border-border object-cover"
          />
        )}

        <div>
          <h1 className="font-display text-3xl">
            {data.displayName || 'Developer'}
          </h1>

          {data.headline && (
            <p className="font-body text-base text-text-muted">
              {data.headline}
            </p>
          )}
        </div>
      </header>

      {data.bio && (
        <p className="whitespace-pre-line font-body text-sm text-text-primary">
          {data.bio}
        </p>
      )}

      {data.github && (
        <section aria-labelledby="pf-github" className="space-y-2">
          <h2 id="pf-github" className="font-display text-lg">
            GitHub
          </h2>

          <p className="font-body text-sm text-text-muted">
            {data.github.repoCount} repositories ·{' '}
            {data.github.totalStars} stars
          </p>

          {data.github.topLanguages.length > 0 && (
            <ul className="flex flex-wrap gap-2">
              {data.github.topLanguages.map((language) => (
                <li
                  key={language}
                  className="rounded-md border border-border px-2 py-1 font-mono text-xs text-text-muted"
                >
                  {language}
                </li>
              ))}
            </ul>
          )}
        </section>
      )}

      {data.projects.length > 0 && (
        <section aria-labelledby="pf-projects" className="space-y-3">
          <h2 id="pf-projects" className="font-display text-lg">
            Projects
          </h2>

          <ul className="space-y-3">
            {data.projects.map((project) => (
              <li
                key={project.name}
                className="border-b border-border pb-3"
              >
                <p className="font-body text-sm font-medium text-text-primary">
                  {project.name}
                </p>

                {project.description && (
                  <p className="font-body text-sm text-text-muted">
                    {project.description}
                  </p>
                )}
              </li>
            ))}
          </ul>
        </section>
      )}

      {data.certificates.length > 0 && (
        <section aria-labelledby="pf-certs" className="space-y-3">
          <h2 id="pf-certs" className="font-display text-lg">
            Certificates
          </h2>

          <ul className="space-y-3">
            {data.certificates.map((cert) => {
              const url = safeHttpUrl(cert.verificationUrl);

              return (
                <li key={`${cert.name}-${cert.issuingOrg}`}>
                  <p className="font-body text-sm font-medium text-text-primary">
                    {cert.name}
                  </p>

                  <p className="font-body text-sm text-text-muted">
                    {cert.issuingOrg}
                    {cert.issueDate ? ` · ${cert.issueDate}` : ''}
                  </p>

                  {url && (
                    <a
                      href={url}
                      target="_blank"
                      rel="noopener noreferrer nofollow"
                      className="font-body text-sm text-signal hover:underline"
                    >
                      Verify
                    </a>
                  )}
                </li>
              );
            })}
          </ul>
        </section>
      )}
    </article>
  );
}