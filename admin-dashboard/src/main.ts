import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { AppComponent } from './app/app.component';
import { ApiKeyInterceptor } from './app/interceptors/api-key.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimations(),
    // ✅ FIX 3: Register the API key interceptor globally
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ApiKeyInterceptor,
      multi: true
    }
  ]
}).catch(err => console.error(err));
