# Changelog

## [0.3.0](https://github.com/google/adk-kotlin/compare/v0.2.1-SNAPSHOT...v0.3.0) (2026-06-12)


### Features

* A2A Agent remote sample added ([ab34dd8](https://github.com/google/adk-kotlin/commit/ab34dd8507cb70aa89c13a173467abde71a28d57))
* Add AgentTransferDemoAgent for demonstrating agent-to-agent transfer ([00a02e2](https://github.com/google/adk-kotlin/commit/00a02e27b3cf875ba0cac42ed81ca11a749cf76c))
* Add GoogleSearchExample to the examples ([09bc61c](https://github.com/google/adk-kotlin/commit/09bc61c79e6755172948b7dcf308d3b7e8114577))
* Add HitlDemoAgent example ([dd75810](https://github.com/google/adk-kotlin/commit/dd758100d653a3c4098a295e139a0988d6592971))
* add Runner.rewindAsync to undo session state and artifacts ([96c6319](https://github.com/google/adk-kotlin/commit/96c63191fa43677a3ea2cd9b7b7467cd7befa098))
* Add TelemetryDemoAgent example ([d9ac998](https://github.com/google/adk-kotlin/commit/d9ac99877aa4d31ef32bf20a36d7ce56bc822561))
* introduce App data class for Kotlin ADK ([e86d9f6](https://github.com/google/adk-kotlin/commit/e86d9f68e0e78d7f7ef2711c6b4489198b9d1613))
* introduce EventCompaction and add it to EventActions ([aeb43b5](https://github.com/google/adk-kotlin/commit/aeb43b533c52299a59a815fc3d37021a7dd6d798))
* introduce EventSummarizer interface ([5fc6b14](https://github.com/google/adk-kotlin/commit/5fc6b147b090daa5cdd437df68a258400652015e))
* introduce LlmEventSummarizer ([9c50e12](https://github.com/google/adk-kotlin/commit/9c50e12ed108db0f47c4fef800394b8cde9449b9))


### Bug Fixes

* honor rewindBeforeInvocationId in HistoryRewriterProcessor ([33195bf](https://github.com/google/adk-kotlin/commit/33195bfe51828a8b7b6b49b7d85c57d96c7b80a6))
* make `NewFileSystemSource.kt` compatible with AndroidSDK 26+ ([04195c1](https://github.com/google/adk-kotlin/commit/04195c10ceb4da49ef7ae761365a75c68de9278d))
* Update tracing in GenaiPrompt: redact prompts and function calls ([a93e5d6](https://github.com/google/adk-kotlin/commit/a93e5d639c31c4dc760a308b53ec6e8a2d1d6310))

## [0.2.0](https://github.com/google/adk-kotlin/compare/v0.1.1...v0.2.0) (2026-05-26)


### Features

* Added structural agent demos: LoopAgentDemo, ParallelAgentDemo, and SequentialAgentDemo ([2a93053](https://github.com/google/adk-kotlin/commit/2a9305317766b094563cee13f7c3823c4738c62e))
* Add a new example agent demonstrating callbacks ([9183250](https://github.com/google/adk-kotlin/commit/918325061412ef62f32c1d958a5927328b9c9bfa))
* Add a new ReportGeneratorAgent example ([c6ea965](https://github.com/google/adk-kotlin/commit/c6ea96552ca259ff047a63cd04eac177ea8daac4))
* Add input schema validation to AgentTool ([3df572f](https://github.com/google/adk-kotlin/commit/3df572f86ef795682b92461e774dca5e8b837f5b))


### Bug Fixes

* exclude protobuf-java transitive dependency in android ([3b2100c](https://github.com/google/adk-kotlin/commit/3b2100c2efd67489f372ad1336cefd5ae7ee0ec0))
* **processor:** support Map&lt;String, Any&gt; and List&lt;Any&gt; as @Tool return types ([26c3f9d](https://github.com/google/adk-kotlin/commit/26c3f9d8231828c323e0329cb0d969f3670e322b))

## 0.1.0 (2026-05-19)

ADK Kotlin 0.1 release. Provides core features for building AI agents on JVM and Android, including:
* LLM agents, custom agents
* Multi - agent orchestration
* Function tools, Agent Skills, and long-running operations
* In-memory session and memory services
* Model integrations: 
  * Gemini on JVM/Android (Google GenAI SDK, Firebase AI),
  * On-device Gemini Nano and Gemma (ML Kit)
* ADK web UI interface

For full details, please visit the official documentation at https://adk.dev.

**Full Changelog**: https://github.com/google/adk-kotlin/commits/v0.1.0
