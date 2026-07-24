import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * ✅ FIX 3: API Key HTTP Interceptor.
 *
 * Automatically injects the X-API-Key header into every outgoing HTTP request
 * so all calls to the backend microservices are authenticated without requiring
 * each component to manually set the header.
 */
@Injectable()
export class ApiKeyInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Skip injecting the key for requests that go to external URLs (e.g. Google Fonts)
    if (!this.isBackendRequest(req.url)) {
      return next.handle(req);
    }

    const authenticatedReq = req.clone({
      headers: req.headers.set('X-API-Key', environment.apiKey)
    });

    return next.handle(authenticatedReq);
  }

  private isBackendRequest(url: string): boolean {
    return url.startsWith('http://localhost:808') || url.startsWith('http://localhost:8000');
  }
}
