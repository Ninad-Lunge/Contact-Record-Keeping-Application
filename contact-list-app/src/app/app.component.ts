import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RouterModule } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NavbarComponent } from './components/navbar/navbar.component';
import { AuthNavbarComponent } from './components/auth-navbar/auth-navbar.component';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule, FontAwesomeModule, NavbarComponent, AuthNavbarComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'contact-list-app';
  showAuthNavbar: boolean = false;

  constructor(private router: Router) {
    this.router.events.subscribe(() => {
      const authRoutes = ['/login', '/register'];
      this.showAuthNavbar = authRoutes.includes(this.router.url);
    });
  }
}
