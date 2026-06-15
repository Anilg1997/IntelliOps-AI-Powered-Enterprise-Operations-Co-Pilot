import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideApollo } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { InMemoryCache } from '@apollo/client/core';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideApollo(() => {
      const httpLink = new HttpLink({ uri: 'http://localhost:8081/api/graphql' });
      return {
        link: httpLink,
        cache: new InMemoryCache(),
        defaultOptions: {
          watchQuery: { fetchPolicy: 'network-first' }
        }
      };
    }),
  ]
};
