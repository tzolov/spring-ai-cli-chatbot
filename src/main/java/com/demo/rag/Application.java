package com.demo.rag;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@SpringBootApplication
public class Application {

	private VectorStore vectorStore;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public Application(VectorStore vectorStore,
			@Value("classpath:rag/wikipedia-hurricane-milton.pdf") Resource hurricaneMilton,
			@Value("classpath:rag/wikipedia-atlantic-hurricane.pdf") Resource hurricane,
			@Value("classpath:rag/wikipedia-tropical-cyclone.pdf") Resource cyclone) {

		// 1. Load the PDF documents into the vector store
		vectorStore.add(new TokenTextSplitter().split(new PagePdfDocumentReader(hurricaneMilton).read()));
		// vectorStore.add(new TokenTextSplitter().split(new
		// PagePdfDocumentReader(hurricane).read()));
		// vectorStore.add(new TokenTextSplitter().split(new
		// PagePdfDocumentReader(cyclone).read()));
		this.vectorStore = vectorStore;
	}

	public static final String ANSI_RESET = "\u001B[0m";

	public static final String ANSI_BOLD = "\u001B[1m";

	public static final String ANSI_YELLOW = "\u001B[33m";

	@Bean
	public CommandLineRunner cli(ChatClient.Builder chatClientBuilder) {

		return args -> {// @formatter:off

			SearchRequest searchRequest = SearchRequest.builder()
				.topK(3)
				.build();

			
			// 2. Create the ChatClient with chat memory and RAG support
			var chatClient = chatClientBuilder
				.defaultSystem("You are useful assistant, expert in hurricanes. Be friendly")
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().maxMessages(500).build()).build()) // Enable chat memory
				.defaultAdvisors(QuestionAnswerAdvisor.builder(this.vectorStore).searchRequest(searchRequest).build()) // Enable RAG
				.build(); // @formatter:on

			// 3. Start the chat loop
			System.out.println("\nI am your Hurricane Milton assistant.\n");
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					System.out.print("\n" + ANSI_YELLOW + "USER: " + ANSI_RESET);
					System.out.println("\n" + ANSI_YELLOW + "ASSISTANT: " + ANSI_RESET
							+ chatClient.prompt(scanner.nextLine()) // Get the user input
								.call()
								.content());
				}
			}
		};
	}

}
