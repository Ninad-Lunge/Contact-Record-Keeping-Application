import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  imports: [CommonModule, RouterModule, FormsModule],
  styleUrls: ['./user-details.component.css']
})
export class UserDetailsComponent implements OnInit {
  user: any;
  
  toastMessage: string = '';
  toastTitle: string = '';
  toastColor: string = '';

  constructor(private userService: UserService, private modalService: NgbModal) {}

  ngOnInit(): void {
    this.loadUser();
  }

  loadUser() {
    this.userService.getUserDetails().subscribe((data) => {
      this.user = data;
    });
  }

  openEditModal(content: any) {
    this.modalService.open(content, { centered: true });
  }  

  updateProfile() {
    this.userService.updateUserProfile(this.user).subscribe(
      () => {
        this.showToast('Success', 'Profile updated successfully!', 'success');
        this.loadUser();
      },
      (error) => {
        this.showToast('Error', 'Failed to update profile.', 'danger');
        console.error('Error updating profile:', error);
      }
    );
  }

  showToast(title: string, message: string, color: string) {
    this.toastTitle = title;
    this.toastMessage = message;
    this.toastColor = color;

    setTimeout(() => {
      this.hideToast();
    }, 5000);
  }

  hideToast() {
    this.toastMessage = '';
  }
}