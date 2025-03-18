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
    this.userService.updateUserProfile(this.user).subscribe(() => {
      alert('Profile updated successfully!');
      this.loadUser();
      // this.closeModal();
    });
  }

  // closeModal() {
  //   const modalElement = document.getElementById('editProfileModal');
  //   if (modalElement) {
  //     const modal = bootstrap.Modal.getInstance(modalElement);
  //     modal.hide();
  //   }
  // }
}
