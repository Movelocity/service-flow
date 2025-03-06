# API Directory

This directory contains all API-related code for the application.

## Purpose

The API directory centralizes all code related to external API communication. This includes:

- API client configuration
- Request and response types
- API endpoints
- Authentication logic
- Error handling

## Structure

- `client.ts` - Base API client configuration (axios, fetch, etc.)
- `endpoints/` - API endpoint definitions organized by domain
- `types/` - TypeScript interfaces for API requests and responses
- `hooks/` - Custom hooks for API data fetching
- `utils/` - Utility functions for API operations

## Usage

Import API functions as needed in your components or stores:

```typescript
import { fetchUsers } from '@/api/endpoints/users';
import type { User } from '@/api/types/user';
import { useApiQuery } from '@/api/hooks/useApiQuery';
``` 