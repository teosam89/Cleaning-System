# Contributing to Cleaning Management System

First off, thank you for considering contributing to the Cleaning Management System! It's people like you that make this system such a great tool.

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to support@cleaningsystem.com.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title**
* **Describe the exact steps which reproduce the problem**
* **Provide specific examples to demonstrate the steps**
* **Describe the behavior you observed after following the steps**
* **Explain which behavior you expected to see instead and why**
* **Include screenshots and animated GIFs** if possible
* **Include your environment details** (OS, browser, Java version, etc.)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title**
* **Provide a step-by-step description of the suggested enhancement**
* **Provide specific examples to demonstrate the steps**
* **Describe the current behavior** and **explain which behavior you expected to see instead**
* **Explain why this enhancement would be useful**

### Pull Requests

* Fill in the required template
* Do not include issue numbers in the PR title
* Include screenshots and animated GIFs in your pull request whenever possible
* Follow the Java and JavaScript/Vue.js styleguides
* Include thoughtfully-worded, well-structured tests
* Document new code
* End all files with a newline

## Development Process

### Setting Up Your Development Environment

1. **Fork the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Cleaning-System.git
   cd Cleaning-System
   ```

2. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Set up the backend**
   ```bash
   cd cleaning-system-backend
   mvn clean install
   ```

4. **Set up the frontend**
   ```bash
   cd cleaning-system-frontend
   npm install
   ```

### Coding Standards

#### Java/Spring Boot (Backend)

* Follow standard Java naming conventions
* Use meaningful variable and method names
* Write JavaDoc comments for public methods
* Keep methods small and focused
* Use Spring Boot best practices
* Write unit tests for new features

Example:
```java
/**
 * Retrieves a task by its ID.
 *
 * @param taskId the ID of the task to retrieve
 * @return the task entity
 * @throws ResourceNotFoundException if task not found
 */
public Task getTaskById(Long taskId) {
    return taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
}
```

#### Vue.js (Frontend)

* Follow Vue.js style guide
* Use composition API when possible
* Write clear component names
* Keep components small and reusable
* Use Vuetify components consistently
* Write unit tests for complex logic

Example:
```vue
<template>
  <v-card>
    <v-card-title>{{ task.title }}</v-card-title>
    <v-card-text>{{ task.description }}</v-card-text>
  </v-card>
</template>

<script>
export default {
  name: 'TaskCard',
  props: {
    task: {
      type: Object,
      required: true
    }
  }
}
</script>
```

### Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line

Examples of good commit messages:
```
Add GPS verification for attendance check-in
Fix task status update bug in supervisor dashboard
Update API documentation for new endpoints
Refactor authentication service for better error handling
```

### Testing

#### Backend Tests
```bash
cd cleaning-system-backend
mvn test
```

#### Frontend Tests
```bash
cd cleaning-system-frontend
npm run test:unit
```

Please ensure all tests pass before submitting a pull request.

### Documentation

* Update the README.md with details of changes to the interface
* Update the API documentation in docs/API.md for any API changes
* Add JSDoc/JavaDoc comments for new functions
* Update the CHANGELOG.md with notable changes

## Project Structure

```
Cleaning-System/
├── cleaning-system-backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cleaningsystem/backend/
│   │   │   │   ├── controller/     # REST controllers
│   │   │   │   ├── service/        # Business logic
│   │   │   │   ├── repository/     # Data access
│   │   │   │   ├── entity/         # JPA entities
│   │   │   │   ├── dto/            # Data transfer objects
│   │   │   │   ├── security/       # Security configuration
│   │   │   │   └── config/         # Spring configuration
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── cleaning-system-frontend/
│   ├── src/
│   │   ├── components/     # Reusable Vue components
│   │   ├── views/          # Page components
│   │   ├── router/         # Vue Router configuration
│   │   ├── utils/          # Utility functions
│   │   └── assets/         # Static assets
│   └── package.json
└── docs/                   # Documentation
```

## Branch Naming Convention

* `feature/` - New features (e.g., `feature/add-task-tags`)
* `fix/` - Bug fixes (e.g., `fix/login-validation`)
* `docs/` - Documentation updates (e.g., `docs/api-endpoints`)
* `refactor/` - Code refactoring (e.g., `refactor/task-service`)
* `test/` - Test additions or modifications (e.g., `test/attendance-service`)

## Pull Request Process

1. **Update documentation** - Ensure all relevant documentation is updated
2. **Add tests** - Include tests for new features
3. **Update CHANGELOG.md** - Add your changes to the Unreleased section
4. **Ensure CI passes** - All tests must pass
5. **Request review** - Tag at least one maintainer for review
6. **Address feedback** - Make requested changes promptly
7. **Squash commits** - Squash commits before merging if requested

## Release Process

1. Update version numbers in `pom.xml` and `package.json`
2. Update CHANGELOG.md
3. Create a release branch
4. Test thoroughly
5. Merge to master
6. Tag the release
7. Deploy to production

## Code Review Guidelines

### For Reviewers

* Be respectful and constructive
* Ask questions rather than making demands
* Praise good code
* Provide specific suggestions
* Approve when satisfied

### For Contributors

* Don't take feedback personally
* Respond to all comments
* Ask for clarification if needed
* Make requested changes promptly
* Thank reviewers for their time

## Community

* **GitHub Discussions**: For questions and discussions
* **GitHub Issues**: For bug reports and feature requests
* **Email**: support@cleaningsystem.com for private inquiries

## Recognition

Contributors will be recognized in:
* README.md Contributors section
* CHANGELOG.md for their contributions
* GitHub contributors page

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Questions?

Don't hesitate to ask questions! Create an issue with the `question` label or reach out to the maintainers directly.

---

**Thank you for contributing to make the Cleaning Management System better!**
