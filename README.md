# ProjetReseau

It is a basic communication application between Servers and Clients using TCP Protocol

The goal of the ChatFusion project is to create a discussion and file exchange service (IRC type, Discord). The specificity of the project is to allow several servers to connect to each other to form a single server. Clients remain connected to their home server but their messages and files are forwarded to all client servers connected to each other. Your ChatFusion protocol must be implemented on top of TCP.

Principle of the application
Clients connect to a server. Each connected client is identified by a pseudonym. The protocol must allow two forms of access: authenticated password access and passwordless access. Assume that the server has access in some form to a database of nicknames and passwords. The protocol does not deal with the creation and updating of this database. The server must guarantee that two clients cannot have the same nickname and that an authenticated client without a password does not use the nickname of a database user. Each server has a name which is fixed when the server is started and cannot be modified. Once connected and identified by a pseudonym, customers can:

send public messages that will be forwarded to all connected clients.
send private messages and files to another client.
We want to be able to continue sending messages while transmitting a file to another user.
Compared to a standard chat server (IRC type), the particularity of this project is that a server A can merge with a server B. Typically in the console of server A, we will enter a merge command by giving an address corresponding to server B. After the merge, all public messages sent by a client of A will be delivered to clients of A and B. Same goes for messages sent by clients of B. To send a private message or a file, the client must give the nickname and server name of the target of the private message. If a client of A and a client of B have the same pseudonym, there will be no problem because they will be distinguished by their server name. The uniqueness constraint of the pseudonym is checked only at the level of a server.

The difficulty comes from the fact that once A has merged with B, it can merge with a server C which has itself merged with a server D. After the merge, we will have to obtain a merge of A,B,C and D.

To simplify the protocol, it will be assumed that if the fusion of two servers is possible (for example that they have two different names), it will be accepted.

Your protocol should allow multiple servers to be started on the same machine. In particular, this prohibits the use of fixed port numbers as is done for certain protocols.
