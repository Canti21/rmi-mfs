# Mondongo File System

An implementation of a distributed file system using Java RMI.

![Imgur](https://i.imgur.com/55spV7U.jpg)

## 📚 Overview
A simple distributed file system designed based on the Google File System, made using pure Java and Remote Method Invocation. Made as our **Distributed Systems** class final project.

## 📽️ Demo Video

[![Watch the demo](https://img.youtube.com/vi/AegmIipnL6Q/0.jpg)](https://www.youtube.com/watch?v=AegmIipnL6Q)

## 🛠️ Arquitecture
This project uses a simple 3-tier architecture:

- **Master Node:** Manages metadata and coordinates operations between the client and data nodes.
- **Data Nodes:** Perform actual file storage and handle file-related tasks.
- **Client:** Acts as the interface for users to interact with the system.

## 🚨 Notes

- This project was developed in less than a week for a school assignment.
- It is **not intended for production use** and operates with hardcoded configurations.
