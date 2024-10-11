# Spring AI Chat Bot CLI

This Spring Boot, CLI, application demonstrates how to create an AI-powered chatbot with domain-specific knowledge (in this case, about Hurricane Milton) using Spring AI, Retrieval-Augmented Generation RAG and Conversational Memory.

Application uses the [Hurricane_Milton](https://en.wikipedia.org/wiki/Hurricane_Milton) wikipage saved as `wikipedia-hurricane-milton-page.pdf`.

## ChatBot Application

quick build run the app.
```
./mvnw clean install
./mvnw spring-boot:run
```

## Auto-configurations

### AI Model

By default, this project uses OpenAI's Spring Boot starter (`spring-ai-openai-spring-boot-starter`). 
However, you can easily switch to any other supported AI model.
The `pom.xml` file prvidew few alternative AI model dependencies. (Note: Most models, except Ollama/Llama3.2, require an API key for access.)
Configure your API key and other model properties in the `application.properties` file.
The [Chat Model API](https://docs.spring.io/spring-ai/reference/api/chatmodel.html) lists all supported modesl.

### Vector Store

The project is configured to use Chroma (`spring-ai-chroma-store-spring-boot-starter`) as a vector store, running locally:
A `docker-compose.yaml` file is provided to start a local Chroma instance.
The project is configured with Spring Boot Docker Compose integration for easy setup. (e.g. you don't have to start the docker-compose manually).
Find more about [Vector Stores](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)

### PDF Document Processing

PDF document reading capability is enabled through the `spring-ai-pdf-document-reader` dependency.
Find more about the Spring AI [document indexing support](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html)

## CommandLineRunner

`CommandLineRunner` created by the `cli` Bean, is a Spring Boot interface for running code after the application context is loaded.
This is the entry point of our chatbot the application.

## Vector Store Loading

```java
vectorStore.add(new TokenTextSplitter().split(new PagePdfDocumentReader(hurricaneDocs).read()));
```

This line reads a PDF document about Hurricane Milton, splits it into tokens, and adds it to a vector store. This is part of the RAG setup, allowing the chatbot to retrieve relevant information.

## ChatClient Configuration

```java
var chatClient = chatClientBuilder
    .defaultSystem("You are useful assistant, expert in hurricanes.")
    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
    .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
    .build();
```

Here, a `ChatClient` is built with the following configurations:
- A system prompt defining the assistant's role
- A `MessageChatMemoryAdvisor` for maintaining conversation history
- A `QuestionAnswerAdvisor` that uses the vector store for RAG capabilities

## Chat Loop

```java
try (Scanner scanner = new Scanner(System.in)) {
    while (true) {
        System.out.print("\nUSER: ");
        System.out.println("\nASSISTANT: " + 
            chatClient.prompt(scanner.nextLine())
                .call()
                .content());
    }
}
```

This creates an infinite loop that:
1. Prompts the user for input
2. Sends the user's input to the chatbot
3. Prints the chatbot's response

The chatbot uses the configured `ChatClient`, which incorporates the conversation history and RAG capabilities to generate responses.

## Key Features

1. **RAG Implementation**: The application uses a vector store to implement RAG, allowing the chatbot to retrieve relevant information from the loaded document.
2. **Conversation Memory**: The `MessageChatMemoryAdvisor` enables the chatbot to remember previous interactions within the conversation.
3. **PDF Document Processing**: The application can read and process PDF documents, making the information available to the chatbot.
4. **Interactive Console Interface**: The application provides a simple console-based interface for interacting with the chatbot.
