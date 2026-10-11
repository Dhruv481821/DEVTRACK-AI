import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';

import { Button } from '@/shared/ui/Button';
import {
  Input,
  Label,
  FieldError,
} from '@/shared/ui/Input';

import { ApiError } from '@/shared/api-client/types';

import {
  useMyPortfolio,
  useUpdateMyPortfolio,
} from '../api/usePortfolio';

import {
  portfolioSettingsSchema,
  type PortfolioSettingsFormValues,
} from '../schemas/portfolioSchemas';

export function PortfolioSettingsForm() {
  const {
    data: settings,
    isLoading,
    isError,
  } = useMyPortfolio();

  const updateMutation = useUpdateMyPortfolio();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<PortfolioSettingsFormValues>({
    resolver: zodResolver(portfolioSettingsSchema),
    defaultValues: {
      slug: '',
      headline: '',
      published: false,
    },
  });

  useEffect(() => {
    if (settings) {
      reset({
        slug: settings.slug ?? '',
        headline: settings.headline ?? '',
        published: settings.published,
      });
    }
  }, [settings, reset]);

  const onSubmit = handleSubmit((values) => {
    updateMutation.mutate(values);
  });

  if (isLoading) {
    return (
      <p className="font-body text-sm text-text-muted">
        Loading portfolio…
      </p>
    );
  }

  if (isError) {
    return (
      <p className="font-body text-sm text-danger">
        Couldn't load your portfolio settings. Please try again.
      </p>
    );
  }

  const conflict =
    updateMutation.error instanceof ApiError &&
    updateMutation.error.status === 409;

  return (
    <form
      onSubmit={onSubmit}
      className="grid grid-cols-1 gap-4"
    >
      <div>
        <Label htmlFor="portfolio-slug">
          Portfolio URL
        </Label>

        <div className="flex items-center gap-2">
          <span className="shrink-0 font-body text-sm text-text-muted">
            {window.location.origin}/u/
          </span>

          <Input
            id="portfolio-slug"
            placeholder="your-name"
            autoComplete="off"
            {...register('slug')}
          />
        </div>

        <FieldError message={errors.slug?.message} />

        {conflict && (
          <p className="mt-1.5 font-body text-sm text-danger">
            That URL is already taken. Try another.
          </p>
        )}
      </div>

      <div>
        <Label htmlFor="portfolio-headline">
          Headline
        </Label>

        <Input
          id="portfolio-headline"
          placeholder="e.g. Full-stack developer"
          {...register('headline')}
        />

        <FieldError message={errors.headline?.message} />
      </div>

      <div className="flex items-start gap-3">
        <input
          id="portfolio-published"
          type="checkbox"
          className="mt-1 h-4 w-4"
          {...register('published')}
        />

        <div>
          <Label
            htmlFor="portfolio-published"
            className="mb-0"
          >
            Publish my portfolio
          </Label>

          <p className="font-body text-sm text-text-muted">
            When published, anyone with the link can see your
            name, bio, projects, certificates and GitHub totals.
            Tasks, notes, repository names and your email are
            never shown.
          </p>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <Button
          type="submit"
          loading={updateMutation.isPending}
        >
          Save
        </Button>

        {settings?.published && settings.slug && (
          <Link
            to={`/u/${settings.slug}`}
            className="font-body text-sm text-signal hover:underline"
          >
            View public page
          </Link>
        )}
      </div>

      {updateMutation.isSuccess && (
        <p className="font-body text-sm text-success">
          Portfolio saved.
        </p>
      )}

      {updateMutation.isError && !conflict && (
        <p className="font-body text-sm text-danger">
          Couldn't save your portfolio. Please try again.
        </p>
      )}
    </form>
  );
}