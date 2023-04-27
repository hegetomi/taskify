# Taskify
### Description
Taskify is a lightweight ticket / task management tool for SMBs and small teams. Not everyone needs a bloated SaaS such as JIRA, however the limits of a shared excel sheet are easily met. Taskify is the perfect solution for everyone to track their tasks, let it be tickets submitted by their end users, or themselves internally.
### Used technologies
Spring Boot,
Spring Security,
Liquibase,
Lombok,
Mapstruct,
Hibernate Envers,
Jacoco
### Features
#### Feature matrix
|   | Admin | Manager | Employee | User | No role |
| ------------ | :------------: | :------------: | :------------: | :------------: | :------------: |
| Register |   |   |   |   | X |
| Login |   |   |   |   | X |
| Get all users | X |   |   |   |   |
| Get current user | X | X | X | X |   |
| Modify password | X | X | X | X |   |
| Modify user rights | X |   |   |   |   |
| Get ticket stats |   | X |   |   |   |
| Get average solve time |   | X |   |   |   |
| Create new ticket |   |   | X | X |   |
| Get posted tickets |   |   | X | X |   |
| Get posted ticket details |   |   | X | X |   |
| Edit posted ticket |   |   | X | X |   |
| Get assigned tickets |   |   | X |   |   |
| Get assigned ticket details |   |   | X |   |   |
| Edit assigned ticket |   |   | X |   |   |
| Get unassigned tickets |   |   | X |   |   |
| Assign ticket to user |   |   | X |   |   |
| Get ticket history by ID |   |   | X |   |   |
| Post comment to ticket |   |   | X | X |   |
| Get comments for ticket |   |   | X | X |   |
| Delete comment by ID |   |   | X | X |   | |

#### Detailed feature description
##### `POST` Register
Register is available to everyone at the current release. When sending a register request, it must contain a `username` field with length between 3 and 20, and a `password` field wich must be at least 6 and up to 12 characters long, and must contain at least one lowercase letter, uppercase letter, a number and a special character. The `username` must be unique, no two users may have the same name.
##### `POST` Login
Login is available to everyone at the current release. The request body requires the `username` and `password` parameters used at register. On successful login, the user will receive a JWT token which may be used to authenticate while it is valid.
##### `GET` Get all users
Only avaliable to admin users. The request only returns the users and their roles, but not their current tickets or comments. This request may be used with in combination Modify user rights for an admin user to view and modify user access.
##### `GET` Get current user
Returns information about the currently logged in user. In case of users with Employee of User roles, the list of submitted and assigned tickets is also sent.
##### `PUT` Modify password
Available to every authenticated user. The old password and the new password must be sent in the request, and if the old password matches with the database, the password is set to the new password.
##### `PUT` Modify user rights
Only avaliable to admins. When the modified user no longer has `EMPLOYEE` role, every assigned ticket to them will be set to unassigned. When the modified user no longer has `USER` or `EMPLOYEE` role, every ticket submitted by them will be set to anonymous.
##### `GET` Get ticket stats
Only avaliable to managers. The request returns a list of every user with `EMPLOYEE` role, and the number of their open / closed tickets based on the `open` request parameter value.
##### `GET` Get average solve time
Only available to managers. Returns a list of every user with `EMPLOYEE` role, and the average time between their assigned tickets' posted and closed date.
##### `POST` Create new ticket
Available to users and employees. The title must be at least 10 characters and up to 75 characters, the description is minimum 50 characters and up to 250 characters. A priority and a ticket type must also be given to the request.
##### `GET` Get posted tickets
Returns every ticket submitted by the currently logged in user.
##### `GET` Get posted ticket details by ID
Returns the details (with comments) of a ticket to the user who posted it.
##### `PUT` Edit posted ticket
The user who posted the ticket can edit the properties of an existing ticket.
##### `GET` Get assigned tickets
Returns every ticket assigned to the currently logged in user.
##### `GET` Get assigned ticket details
Returns the details (with comments) of a ticket to the assignee.
##### `PUT` Edit assigned ticket
The ticket assignee can edit the properties of an existing ticket. Only the assignee is able to close a ticket by setting its status to `DONE`. In this case, the ticket's closedDate property is set. If the ticket is set from `DONE` to anything else, the closedDate is set to `null`.
##### `GET` Get unassigned tickets
Returns the list of all unassigned tickets to a manager or employee.
##### `PUT` Assign ticket to user
Assigns a ticket to a user. Only possible if the ticket is not yet assigned, or the assignee is the one who is initiates the reassignment. The old assignee may no longer access the ticket after reassignment.
##### `GET` Get ticket history by ID
Returns every change made to the ticket with a timestamp recorded by Hibernate Envers.
##### `POST` Post comment to ticket
Ticket poster or assignee may post a comment to the ticket.
##### `GET` Get comments for ticket - DEPRECIATED
Ticket poster or assignee may query the comments for a ticket.
##### `DELETE` Delete comment by ID
Ticket poster or assignee can delete a comment posted for a ticket.

### Ideas not yet implemented
#### Openapi generator
Openapi Generator would generate Controller interfaces and DTO-s based on the openapi specification, so the developer could focus on the business logic.
#### Message queue
Implementing an external Artemis queue would enable to receive anonymous tickets even when the main application is offline. This would make sense if there is a product used by end users, who would report bugs, but have no account.
#### Scheduled user and employee synchronisation
Register feature could be turned off, if there was some kind of an integration to a CRM (to have the list of users) and a HR system or similar (to have the employee list). Even a user group from HPSM would suffice. The integration could be done via WSDL services, scheduled by cron.
