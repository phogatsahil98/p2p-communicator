# Encrypted P2P Tactical Communicator

A secure, multi-threaded peer-to-peer chat application built entirely in core Java. It bypasses central servers by allowing direct IP-to-IP socket connections and secures all traffic using AES-256 encryption.

## Features
* **Decentralized Networking:** Operates without a central server using `java.net.ServerSocket` and `java.net.Socket`.
* **Out-of-Band Key Exchange:** Manual exchange of Base64-encoded AES session keys ensures secure handshakes over untrusted networks.
* **Asynchronous I/O:** Utilizes custom background threads to handle incoming data streams without blocking the user interface.
* **Terminal UI:** Color-coded command-line interface using ANSI escape sequences.

## Tech Stack
* **Language:** Java 11+
* **Networking:** Core Java Sockets API
* **Security:** `javax.crypto` (AES-256)
* **Concurrency:** `java.lang.Thread`

## Author
* Sahil Phougat