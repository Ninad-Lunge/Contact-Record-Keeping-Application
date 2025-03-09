import { Component, OnInit } from '@angular/core';
import { ContactService } from '../../services/contact.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-contacts',
  templateUrl: './contacts.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./contacts.component.css']
})
export class ContactsComponent implements OnInit {
  contacts: any[] = [];
  selectedContact = { contactId: null, firstName: '', lastName: '', email: '', phone: '' };
  isEditing: boolean = false;
  currentPage: number = 1;
  pageSize: number = 5;

  constructor(private contactService: ContactService) {}

  ngOnInit(): void {
    this.loadContacts();
  }

  loadContacts(): void {
    this.contactService.getContacts().subscribe(
      (data) => { this.contacts = data; },
      (error) => { console.error('Error fetching contacts:', error); }
    );
  }

  addContact(): void {
    this.contactService.createContact(this.selectedContact).subscribe(() => {
      this.loadContacts();
      this.clearForm();
    });
  }

  editContact(contact: any): void {
    this.selectedContact = { ...contact };
    this.isEditing = true;
  }

  updateContact(): void {
    if (this.selectedContact.contactId === null) return;
    
    this.contactService.updateContact(this.selectedContact.contactId, this.selectedContact).subscribe(() => {
      this.loadContacts();
      this.clearForm();
    });
  }

  deleteContact(contactId: number | undefined): void {
    if (!contactId) {
      console.error("Invalid contactId: ", contactId);
      return;
    }
    this.contactService.deleteContact(contactId).subscribe(() => {
      this.loadContacts();
    });
  }

  deleteAllContacts(): void {
    this.contacts.forEach(contact => {
      this.contactService.deleteContact(contact.contactId).subscribe(() => {
        this.loadContacts();
      });
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
  }

  // Filters
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
}
