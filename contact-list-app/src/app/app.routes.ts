import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ContactsComponent } from './components/contacts/contacts.component';
import { UserDetailsComponent } from './components/user-details/user-details.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', component: ContactsComponent },
  { path: 'contacts', component: ContactsComponent },
  { path: 'user-details', component: UserDetailsComponent},
  { path: '', redirectTo: '', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];