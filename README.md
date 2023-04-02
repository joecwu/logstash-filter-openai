# OpenAI Filter Logstash Plugin

The OpenAI Filter Logstash Plugin is a Java-based filter plugin for Logstash, which allows you to process events using OpenAI's API. It is designed to be highly configurable and versatile, enabling users to adapt the plugin to their specific use cases.

## Key Features
1. Configurable settings:
  - OpenAI API key
  - Prompt template with support for variables enclosed in {{ }}, which are replaced with actual data from the event
  - Default values for variables
  - Source field from which the data is taken
  - Target field to store the processed result
2. Supports parsing multi-valued responses, splitting them using a configurable separator, and converting them into an array within the filter.
3. Implements error handling with a configurable retry mechanism.

## Use Cases
The OpenAI Filter Logstash Plugin is useful for processing and enriching log data, textual information, or other types of events using the capabilities of OpenAI's GPT models. Some examples include:

- Analyzing log messages to extract additional insights or summarizing their content
- Categorizing or tagging events based on their content
- Generating natural language responses or explanations for specific events

With its highly configurable nature, the OpenAI Filter Logstash Plugin can be easily adapted to a wide range of applications, making it a valuable addition to your Logstash processing pipeline.