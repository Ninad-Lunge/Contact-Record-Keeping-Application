import { Component, OnInit, ViewChild } from '@angular/core';
import { ContactService } from '../../services/contact.service';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';

@Component({
  selector: 'app-contacts',
  templateUrl: './contacts.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./contacts.component.css']
})
export class ContactsComponent implements OnInit {
  @ViewChild('contactForm') contactForm!: NgForm;
  
  contacts: any[] = [];
  selectedContact = { contactId: null, firstName: '', lastName: '', email: '', phone: '' };
  isEditing: boolean = false;
  currentPage: number = 1;
  pageSize: number = 5;

  toastMessage: string = '';
  toastTitle: string = '';
  toastColor: string = '';

  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private contactService: ContactService) {}

  ngOnInit(): void {
    this.loadContacts();
  }

  loadContacts(): void {
    this.isLoading = true;
    this.contactService.getContacts().subscribe(
      (data) => { 
        this.contacts = data;
        this.isLoading = false;
      },
      (error) => { 
        this.errorMessage = 'Failed to load contacts. Please try again later.';
        this.isLoading = false;
        console.error('Error fetching contacts:', error);
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

  addContact(): void {
    if (!this.validateContact(this.selectedContact)) return;
    
    this.contactService.createContact(this.selectedContact).subscribe(
      () => {
        this.loadContacts();
        this.clearForm();
        this.showToast('Success', 'Contact added successfully!', 'success');
      },
      (error) => {
        if (error.error?.error?.includes('already exists')) {
          this.errorMessage = 'A contact with this phone number already exists.';
          this.showToast('Error', this.errorMessage, 'danger');
        } else {
          this.errorMessage = 'Failed to add contact. Please check your input and try again.';
          this.showToast('Error', 'Failed to add contact.', 'danger');
        }
        console.error('Error adding contact:', error);
      }
    );
  }
  
  editContact(contact: any): void {
    this.selectedContact = { ...contact };
    this.isEditing = true;
  }
  
  updateContact(): void {
    if (this.selectedContact.contactId === null || !this.validateContact(this.selectedContact)) return;

    this.contactService.updateContact(this.selectedContact.contactId, this.selectedContact).subscribe(
      () => {
        this.loadContacts();
        this.clearForm();
        this.showToast('Success', 'Contact updated successfully!', 'success');
      },
      (error) => {
        this.errorMessage = 'Failed to update contact. Please check your input and try again.';
        this.showToast('Error', 'Failed to update contact.', 'danger');
        console.error('Error updating contact:', error);
      }
    );
  }
  
  deleteContact(contactId: number | undefined): void {
    if (!contactId) {
      this.errorMessage = 'Invalid contact ID for deletion.';
      console.error("Invalid contactId: ", contactId);
      return;
    }
    
    this.contactService.deleteContact(contactId).subscribe(
      () => {
        this.loadContacts();
        this.showToast('Success', 'Contact deleted successfully!', 'success');
      },
      (error) => {
        this.errorMessage = 'Failed to delete contact. Please try again later.';
        this.showToast('Error', 'Failed to delete contact.', 'danger');
        console.error('Error deleting contact:', error);
      }
    );
  }
  
  deleteAllContacts(): void {
    let deleteRequests = this.contacts.map(contact => 
      this.contactService.deleteContact(contact.contactId).toPromise()
    );
  
    Promise.all(deleteRequests)
      .then(() => {
        this.loadContacts();
        this.showToast('Success', 'All contacts deleted successfully!', 'success');
      })
      .catch((error) => {
        this.errorMessage = 'Failed to delete all contacts. Please try again later.';
        this.showToast('Error', 'Failed to delete all contacts.', 'danger');
        console.error('Error deleting all contacts:', error);
      });
  }  

  get paginatedContacts() {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.contacts.slice(start, start + this.pageSize);
  }

  getPageNumbers(): number[] {
    return Array.from({ length: Math.ceil(this.contacts.length / this.pageSize) }, (_, i) => i + 1);
  }

  changePage(page: number): void {
    this.currentPage = page;
  }

  clearForm(): void {
    this.selectedContact = { contactId: null, firstName: '', lastName: '', email: '', phone: '' };
    this.isEditing = false;
    this.errorMessage = ''; // Clear error message when resetting form
    
    // Reset form validation state if form is available
    if (this.contactForm) {
      this.contactForm.resetForm();
    }
  }

  filters = { name: '', email: '', phone: '' };

  get filteredContacts() {
    const filtered = this.contacts.filter(contact => {
      return (
        (this.filters.name === '' || contact.fullName.toLowerCase().includes(this.filters.name.toLowerCase())) &&
        (this.filters.email === '' || contact.email.toLowerCase().includes(this.filters.email.toLowerCase())) &&
        (this.filters.phone === '' || contact.phone.includes(this.filters.phone))
      );
    });
  
    const start = (this.currentPage - 1) * this.pageSize;
    return filtered.slice(start, start + this.pageSize);
  }  

  validateContact(contact: any): boolean {
    if (!contact.firstName || !contact.lastName || !contact.email || !contact.phone) {
      this.errorMessage = 'All fields are required.';
      return false;
    }

    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(contact.email)) {
      this.errorMessage = 'Invalid email format.';
      return false;
    }

    if (!/^\d{10}$/.test(contact.phone)) {
      this.errorMessage = 'Phone number must be 10 digits.';
      return false;
    }

    this.errorMessage = '';
    return true;
  }
}