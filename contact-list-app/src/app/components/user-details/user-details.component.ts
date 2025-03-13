import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  imports: [CommonModule, RouterModule],
  styleUrls: ['./user-details.component.css']
})
export class UserDetailsComponent implements OnInit {
  user: any;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService.getUserDetails().subscribe(
      (data) => {
        this.user = data;
      },
      (error) => {
        console.error('Error fetching user details', error);
      }
    );
  }
}
