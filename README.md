# CSCI-348-748-Computer-Network
The goal of this project is to implement a recursive DNS resolver that mimics the hierarchical structure of real-world DNS servers. Each group member will simulate a DNS server at a specific level in the hierarchy (ex: root, TLD, or authoritative). The recursive resolver will query these servers in sequence to resolve a domain name into a usable IP address.

## Architecture Overview
The project is structured into two main parts:

1. **Recursive Resolver**:
   - Acts as the client that initiates the DNS resolution process.
   - Queries DNS servers in a hierarchical order until the desired IP address is resolved.
   - Implements recursive behavior by following responses from the servers to the next level.

2. **DNS Servers**:
   - Each group member hosts a server that represents a specific level in the DNS hierarchy.
   - The hierarchy is as follows:
     - **Root Server**: Handles queries for top-level domains (TLDs) such as `.com`, `.org`, `.edu`, `.eu`, `.ca`, `.io` etc.
     - **TLD Servers**: Handle queries for second-level domains under their respective TLD (e.g., `test.com`).
     - **Authoritative Servers**: Provide the final resolution for fully qualified domain names (e.g., `www.test.com`).
   - Each server:
     - Listens for incoming DNS queries.
     - Consults a database to determine the next server to query or the resolved IP.
     - Responds with the next server's information or the final resolution.

## Database Structure
The hierarchy of servers and their roles is stored in a database. The database schema includes:

| **Field**       | **Description**                            |
|------------------|--------------------------------------------|
| `ID`            | Unique identifier for the record.          |
| `Role`          | The server's role (Root, TLD, Authoritative). |
| `Domain`        | The domain or TLD this server is responsible for. |
| `NextServerIP`  | The IP address of the next server in the hierarchy. |
| `ResolvedIP`    | The resolved IP address (for authoritative servers). |

### Example Records
| **ID** | **Role**       | **Domain**       | **NextServerIP** | **ResolvedIP** |
|--------|-----------------|------------------|------------------|----------------|
| 1      | Root           | N/A              | (TLD Server IP)  | N/A            |
| 2      | TLD Server     | .com             | (Auth Server IP) | N/A            |
| 3      | TLD Server     | .org             | (Auth Server IP) | N/A            |
| 4      | Authoritative  | test.com      | N/A              | 12.345.676.99  |
| 5      | Authoritative  | test.orn      | N/A              | 99.876.543.21  |

## Workflow
### Query Process
1. **Recursive Resolver**:
   - Sends a DNS query to the root server.
   - Receives a response indicating the next server to query.
2. **Root Server**:
   - Identifies the TLD server responsible for the query.
   - Responds with the TLD server's information.
3. **TLD Server**:
   - Identifies the authoritative server for the requested domain.
   - Responds with the authoritative server's information.
4. **Authoritative Server**:
   - Resolves the domain to an IP address.
   - Sends the final IP address back to the recursive resolver.

### Example Resolution
To resolve `www.test.com`:
1. The recursive resolver queries the root server.
2. The root server directs the query to the `.com` TLD server.
3. The TLD server directs the query to the authoritative server for `test.com`.
4. The authoritative server resolves `www.test.com` to `12.345.676.99` and returns the result.

## Project Deliverables
- **Recursive Resolver**: A program that implements the recursive resolution logic.
- **DNS Servers**: Programs hosted by each group member to simulate the hierarchy.
- **Database**: A centralized or distributed database storing the DNS hierarchy.
- **Documentation**: Detailed explanation of the implementation, architecture, and testing process.

## Testing and Validation
- Test the system with mock domains to ensure correct resolution.
- Validate responses at each level of the hierarchy.
- Implement logging to trace the query flow.

## Future Enhancements
- Add support for other DNS record types (e.g., MX, CNAME).
- Implement caching in the recursive resolver for faster performance.
- Introduce fault tolerance for server failures.

## Conclusion
This project provides hands-on experience with DNS resolution mechanics, distributed systems, and hierarchical data structures. By simulating a DNS hierarchy, it replicates the functionality of real-world DNS systems in a controlled environment.
