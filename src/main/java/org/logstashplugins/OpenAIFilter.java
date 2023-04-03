package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@LogstashPlugin(name = "openai")
public class OpenAIFilter implements Filter {

    public static final PluginConfigSpec<String> API_KEY_CONFIG =
            PluginConfigSpec.stringSetting("api_key", "", true, false);

    public static final PluginConfigSpec<String> PROMPT_CONFIG =
            PluginConfigSpec.stringSetting("prompt", "", true, false);

    public static final PluginConfigSpec<Map<String, Object>> DEFAULT_VALUES_CONFIG =
            PluginConfigSpec.hashSetting("default_values", new HashMap<>(), false, false);

    public static final PluginConfigSpec<String> SOURCE_FIELD_CONFIG =
            PluginConfigSpec.stringSetting("source", "message", false, false);

    public static final PluginConfigSpec<String> TARGET_FIELD_CONFIG =
            PluginConfigSpec.stringSetting("target", "result", false, false);

    public static final PluginConfigSpec<String> SEPARATOR_CONFIG =
            PluginConfigSpec.stringSetting("separator", ",", false, false);

    private String id;
    private String apiKey;
    private String prompt;
    private Map<String, Object> defaultValues;
    private String sourceField;
    private String targetField;
    private String separator;

    public OpenAIFilter(String id, Configuration config, Context context) {
        this.id = id;
        this.apiKey = config.get(API_KEY_CONFIG);
        this.prompt = config.get(PROMPT_CONFIG);
        this.defaultValues = config.get(DEFAULT_VALUES_CONFIG);
        this.sourceField = config.get(SOURCE_FIELD_CONFIG);
        this.targetField = config.get(TARGET_FIELD_CONFIG);
        this.separator = config.get(SEPARATOR_CONFIG);
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        for (Event event : events) {
            // Replace variables and call OpenAI API
            String interpolatedPrompt = replaceVariables(prompt, event);
            String response = callOpenAIApi(interpolatedPrompt);

            // Parse response and split it into an array
            String[] result = response.split(separator);

            // Set the result to the target field
            event.setField(targetField, result);
            matchListener.filterMatched(event);
        }
        return events;
    }

    private String replaceVariables(String input, Event event) {
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            Object fieldValue = event.getField(entry.getKey());
            if (fieldValue == null) {
                fieldValue = entry.getValue();
            }
            input = input.replace("{{" + entry.getKey() + "}}", fieldValue.toString());
        }
        return input;
    }

    private String callOpenAIApi(String prompt) {
        // Implement OpenAI API call using OkHttpClient and pass the prompt
        // You can refer to OpenAI API documentation: https://beta.openai.com/docs/

        // Replace this with your OpenAI API URL
        String openaiApiUrl = "https://api.openai.com/v1/engines/davinci-codex/completions";

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        Gson gson = new Gson();

        // Prepare request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("prompt", prompt);
        requestBodyMap.put("max_tokens", 50); // You can adjust the number of tokens based on your requirement
        String requestBodyJson = gson.toJson(requestBodyMap);

        // Create request
        Request request = new Request.Builder()
                .url(openaiApiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(JSON, requestBodyJson))
                .build();

        String responseText;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Handle error and retry mechanism
                throw new IOException("Unexpected code " + response);
            }

            // Parse response
            String responseBody = response.body().string();

            Map<String, Object> responseMap = gson.fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());
            List<Map<String, Object>> choicesList = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> choices = choicesList.get(0);
            responseText = (String) choices.get("text");
            // Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);
            // Map<String, Object> choices = (Map<String, Object>) ((List) responseMap.get("choices")).get(0);
            // responseText = (String) choices.get("text");
        } catch (IOException e) {
            // Handle exceptions and implement retry mechanism if needed
            e.printStackTrace();
            responseText = "";
        }

        return responseText;
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return List.of(API_KEY_CONFIG, PROMPT_CONFIG, DEFAULT_VALUES_CONFIG, SOURCE_FIELD_CONFIG, TARGET_FIELD_CONFIG, SEPARATOR_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }
}