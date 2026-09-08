/**
 * React component for the Natural Language query popup in the LAND IT extension.
 */
import { useEffect, useRef, useState } from "react";
import { api } from "landit-extensions-sdk";
import { Button } from "primereact/button";
import { InputTextarea } from "primereact/inputtextarea";
import { Checkbox } from "primereact/checkbox";
import { InputNumber } from "primereact/inputnumber";
import { Slider } from "primereact/slider";
import styles from "./nlQueryPopup.module.css";
import { Toast } from "primereact/toast";

interface ClarificationOption {
    id: string;
    label: string;
    value: string;
}

interface ClarificationData {
    question: string;
    type: 'multi_choice' | 'numeric_threshold' | 'free_text';
    options?: ClarificationOption[];
    unit?: string;
    topic?: string;
}

interface HistoryMessage {
    id: string;
    timestamp: string;
    type: 'user' | 'agent';
    content: string;
    status?: 'success' | 'error' | 'info' | 'warning';
    details?: {
        clarificationQuestion?: string;
        error?: string;
    };
}

// Enviado ao backend (modules.ClarificationExchange): apenas topic/question/answer.
interface ClarificationExchange {
    topic: string;
    question: string;
    answer: string;
}

interface ScenarioVersionInfo {
    scenarioId: number;
    version: number;
    title: string;
    description?: string;
    versionName: string;
    versionDescription?: string;
    aigp?: string;
    parentVersion?: number | null;
    isOwner?: boolean;
}

const MAX_SLIDER_VALUE = 1000000;

export function NLQueryPopup() {
    const toast = useRef<Toast>(null);
    const historyEndRef = useRef<HTMLDivElement>(null);
    const [scenarioInfo, setScenarioInfo] = useState<ScenarioVersionInfo | null>(null);
    const [query, setQuery] = useState("");
    const [originalQuery, setOriginalQuery] = useState<string | null>(null);
    const [clarificationData, setClarificationData] = useState<ClarificationData | null>(null);
    const [history, setHistory] = useState<HistoryMessage[]>([]);
    const [clarificationExchanges, setClarificationExchanges] = useState<ClarificationExchange[]>([]);
    const [isProcessing, setIsProcessing] = useState(false);

    // Multi-choice state
    const [selectedOptions, setSelectedOptions] = useState<string[]>([]);
    const [freeTextValue, setFreeTextValue] = useState<string>("");
    const [useFreeText, setUseFreeText] = useState<boolean>(false);

    // Numeric threshold state
    const [lowerBound, setLowerBound] = useState<number>(0);
    const [upperBound, setUpperBound] = useState<number | null>(null);

    // Free text state
    const [userResponse, setUserResponse] = useState("");

    const formatNumber = (value: number): string => {
        return new Intl.NumberFormat('pt-PT').format(value);
    };

    // Scroll para o fim do histórico quando há novas mensagens
    useEffect(() => {
        if (historyEndRef.current) {
            historyEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [history]);

    const getSelectedLabels = (values: string[]): string => {
        if (!clarificationData?.options) return values.join(', ');
        const labels = values.map(v => {
            const opt = clarificationData.options?.find(o => o.value === v);
            return opt ? opt.label : v;
        });
        return labels.join(', ');
    };

    const isAllOption = (opt: ClarificationOption): boolean => {
        return opt.id === 'all' ||
            opt.label.toLowerCase().includes('todas') ||
            opt.label.toLowerCase().includes('todos');
    };

    const getNonAllOptions = (): ClarificationOption[] => {
        return clarificationData?.options?.filter(o => !isAllOption(o)) || [];
    };

    const getAllOptionValue = (): string | undefined => {
        return clarificationData?.options?.find(o => isAllOption(o))?.value;
    };

    const updateAllOption = (newSelected: string[]) => {
        const allValue = getAllOptionValue();
        if (!allValue) return newSelected;

        const nonAllValues = getNonAllOptions().map(o => o.value);
        const allNonAllSelected = nonAllValues.every(v => newSelected.includes(v));

        if (allNonAllSelected && !newSelected.includes(allValue)) {
            return [...newSelected, allValue];
        } else if (!allNonAllSelected && newSelected.includes(allValue)) {
            return newSelected.filter(v => v !== allValue);
        }
        return newSelected;
    };

    const addHistoryMessage = (message: Omit<HistoryMessage, 'id' | 'timestamp'>) => {
        const newMessage: HistoryMessage = {
            ...message,
            id: crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(),
            timestamp: new Date().toISOString()
        };
        setHistory(prev => [...prev, newMessage]);
    };

    const formatUserResponseForDisplay = (): string => {
        if (!clarificationData) return "";

        if (clarificationData.type === 'multi_choice') {
            return useFreeText ? freeTextValue.trim() : getSelectedLabels(selectedOptions);
        }

        if (clarificationData.type === 'numeric_threshold') {
            const unit = clarificationData.unit || '';
            const min = lowerBound;
            const max = upperBound;
            const isMinDefault = min === 0;
            const hasMax = max !== null && max !== undefined;
            const parts: string[] = [];
            if (!isMinDefault) {
                parts.push(`Lim. inferior: ${formatNumber(min)} ${unit}`);
            }
            if (hasMax) {
                parts.push(`Lim. superior: ${formatNumber(max)} ${unit}`);
            }
            return parts.length > 0 ? parts.join(', ') : 'Nenhum limite definido';
        }

        return userResponse;
    };

    const buildUserResponseRaw = (): string => {
        if (!clarificationData) return "";
        if (clarificationData.type === 'multi_choice') {
            return useFreeText ? freeTextValue.trim() : selectedOptions.join(', ');
        }
        if (clarificationData.type === 'numeric_threshold') {
            return JSON.stringify({ min: lowerBound, max: upperBound });
        }
        return userResponse;
    };

    useEffect(() => {
        api.getScenarioVersion()
            .then((info: any) => {
                setScenarioInfo(info as ScenarioVersionInfo);
            })
            .catch(() => {});
    }, []);

    const resetToInitial = () => {
        setOriginalQuery(null);
        setClarificationData(null);
        setSelectedOptions([]);
        setFreeTextValue("");
        setUseFreeText(false);
        setLowerBound(0);
        setUpperBound(null);
        setUserResponse("");
        setQuery("");
        setHistory([]);
        setClarificationExchanges([]);
    };

    const resetClarification = () => {
        setOriginalQuery(null);
        setClarificationData(null);
        setSelectedOptions([]);
        setFreeTextValue("");
        setUseFreeText(false);
        setLowerBound(0);
        setUpperBound(null);
        setUserResponse("");
    };

    const isResponseValid = (): boolean => {
        if (!clarificationData) return false;
        if (clarificationData.type === 'multi_choice') {
            return useFreeText ? freeTextValue.trim().length > 0 : selectedOptions.length > 0;
        }
        if (clarificationData.type === 'numeric_threshold') {
            return lowerBound >= 0 && (upperBound === null || upperBound >= lowerBound);
        }
        return userResponse.trim().length > 0;
    };

    const execute = () => {
        if (!clarificationData && !query.trim()) {
            toast.current?.show({ severity: "warn", summary: "Aviso", detail: "Por favor, escreva uma query." });
            return;
        }
        if (clarificationData && !isResponseValid()) {
            toast.current?.show({ severity: "warn", summary: "Aviso", detail: "Por favor, responda à pergunta." });
            return;
        }

        const currentQuery = query.trim();
        const isClarification = !!clarificationData;

        if (!isClarification) {
            setClarificationExchanges([]);
        }

        let updatedExchanges = [...clarificationExchanges];

        if (isClarification && clarificationData) {
            const rawAnswer = buildUserResponseRaw();
            const displayAnswer = formatUserResponseForDisplay();
            const pendingExchange: ClarificationExchange = {
                topic: clarificationData.topic || 'GENERIC_TEXT',
                question: clarificationData.question,
                answer: rawAnswer || displayAnswer
            };
            updatedExchanges = [...clarificationExchanges, pendingExchange];
            setClarificationExchanges(updatedExchanges);
        }

        const finalHistory = isClarification ? updatedExchanges : [];

        const userDisplayMessage = isClarification ? formatUserResponseForDisplay() : currentQuery;
        addHistoryMessage({
            type: 'user',
            content: userDisplayMessage || currentQuery
        });

        if (!isClarification) {
            setQuery('');
        }

        setIsProcessing(true);

        const effectiveQuery = clarificationData ? (originalQuery ?? currentQuery) : currentQuery;

        const input = {
            scenario: scenarioInfo?.scenarioId,
            version: scenarioInfo?.version,
            query: effectiveQuery,
            clarificationHistory: finalHistory
        };

        api.executeFunctionModule("NL-Filter-Extension", "NL-Query-Function", input)
            .then((res: any) => {
                if (!res) {
                    const errorMessage = 'Resposta vazia do servidor.';
                    addHistoryMessage({
                        type: 'agent',
                        content: `❌ Erro: ${errorMessage}`,
                        status: 'error',
                        details: { error: errorMessage }
                    });
                    toast.current?.show({ severity: "error", summary: "Erro", detail: errorMessage });
                    setIsProcessing(false);
                    return;
                }

                // --- CASO 1: CLARIFICAÇÃO ---
                if (res.clarificationQuestion) {
                    setOriginalQuery(input.query);
                    setClarificationData({
                        question: res.clarificationQuestion,
                        type: res.clarificationType || 'free_text',
                        options: res.clarificationOptions || [],
                        unit: res.unit || '',
                        topic: res.clarificationTopic || 'GENERIC_TEXT'
                    });

                    addHistoryMessage({
                        type: 'agent',
                        content: res.clarificationQuestion,
                        status: 'info',
                        details: {
                            clarificationQuestion: res.clarificationQuestion
                        }
                    });

                    if (res.clarificationType === 'numeric_threshold') {
                        setLowerBound(0);
                        setUpperBound(null);
                    }

                    setSelectedOptions([]);
                    setFreeTextValue("");
                    setUseFreeText(false);
                    setUserResponse("");
                    setIsProcessing(false);
                    return;
                }

                // --- CASO 2: SUCESSO ---
                if (res.success) {
                    setClarificationExchanges([]);
                    resetClarification();

                    const successMessage = res.message || 'Filtro criado com sucesso!';
                    addHistoryMessage({
                        type: 'agent',
                        content: `✅ ${successMessage}`,
                        status: 'success'
                    });

                    // 🔥 MENSAGEM ADICIONAL APÓS O SUCESSO
                    addHistoryMessage({
                        type: 'agent',
                        content: "O filtro está a ser executado.",
                        status: 'info'
                    });

                    api.notifyChange('filters');

                    // NOTIFICAÇÃO POP-UP REMOVIDA
                    setIsProcessing(false);
                    return;
                }

                // --- CASO 3: ERRO ---
                const errorMessage = res.message || 'Ocorreu um erro ao criar o filtro.';
                addHistoryMessage({
                    type: 'agent',
                    content: `❌ Erro: ${errorMessage}`,
                    status: 'error',
                    details: {
                        error: errorMessage
                    }
                });

                toast.current?.show({
                    severity: "error",
                    summary: "Erro",
                    detail: errorMessage
                });
                setIsProcessing(false);
            })
            .catch((err) => {
                const errorMessage = err.message || 'Falha na comunicação com o servidor.';
                addHistoryMessage({
                    type: 'agent',
                    content: `❌ Erro: ${errorMessage}`,
                    status: 'error',
                    details: {
                        error: errorMessage
                    }
                });

                toast.current?.show({
                    severity: "error",
                    summary: "Erro",
                    detail: errorMessage
                });
                setIsProcessing(false);
            });
    };

    const handleMultiChoiceChange = (optValue: string, checked: boolean) => {
        const allValue = getAllOptionValue();
        const isAll = optValue === allValue;

        let newSelected: string[];
        if (isAll) {
            newSelected = checked
                ? (clarificationData?.options?.map(o => o.value) || [])
                : [];
        } else {
            newSelected = checked
                ? [...selectedOptions, optValue]
                : selectedOptions.filter(v => v !== optValue);
            newSelected = updateAllOption(newSelected);
        }

        setSelectedOptions(newSelected);
        if (checked) setUseFreeText(false);
    };

    const clearHistory = () => {
        setHistory([]);
        setClarificationExchanges([]);
    };

    return (
        <div className={styles.nlQueryPopupDiv}>
            <Toast ref={toast} />

            <div className={styles.header}>
                <div className={styles.contextInfo}>
                    <span className={styles.contextLabel}>
                        <strong>Cenário:</strong> {scenarioInfo?.title ?? '...'}
                    </span>
                    <span className={styles.contextLabel}>
                        <strong>Versão:</strong> {scenarioInfo?.versionName ?? '...'}
                    </span>
                </div>
                <div className={styles.headerActions}>
                    {history.length > 0 && (
                        <Button
                            label="Limpar histórico"
                            icon="pi pi-trash"
                            severity="secondary"
                            text
                            size="small"
                            onClick={clearHistory}
                            disabled={isProcessing}
                            className={styles.clearHistoryButton}
                        />
                    )}
                    <Button
                        label="Voltar"
                        icon="pi pi-arrow-left"
                        severity="secondary"
                        text
                        size="small"
                        onClick={resetToInitial}
                        disabled={isProcessing}
                        className={styles.resetButton}
                    />
                </div>
            </div>

            <div className={styles.historyContainer}>
                {history.length === 0 && (
                    <div className={styles.emptyHistory}>
                        <span className={styles.emptyIcon}>💬</span>
                        <span className={styles.emptyText}>Nenhuma interação ainda.</span>
                        <span className={styles.emptySubtext}>Escreva uma query para começar.</span>
                    </div>
                )}
                {history.map((msg) => (
                    <div key={msg.id} className={`${styles.message} ${styles[msg.type]}`}>
                        <div className={styles.messageHeader}>
                            <span className={styles.messageAvatar}>
                                {msg.type === 'user' ? '👤' : '🤖'}
                            </span>
                            <span className={styles.messageTimestamp}>
                                {new Date(msg.timestamp).toLocaleTimeString('pt-PT')}
                            </span>
                        </div>
                        <div className={styles.messageContent}>
                            {msg.content}
                        </div>
                        {msg.type === 'agent' && msg.status === 'info' && msg.details?.clarificationQuestion && (
                            <div className={styles.messageClarification}>
                                <div className={styles.clarificationHint}>
                                    <span className={styles.hintIcon}>❓</span>
                                    {clarificationData?.type !== 'free_text' && (
                                        <span className={styles.hintText}>Selecione uma opção abaixo para responder</span>
                                    )}
                                    {clarificationData?.type === 'free_text' && (
                                        <span className={styles.hintText}>Escreva a sua resposta</span>
                                    )}
                                </div>
                            </div>
                        )}
                        {msg.type === 'agent' && msg.status === 'error' && msg.details?.error && (
                            <div className={styles.messageError}>
                                <span className={styles.errorText}>{msg.details.error}</span>
                            </div>
                        )}
                    </div>
                ))}
                {isProcessing && (
                    <div className={`${styles.message} ${styles.agent} ${styles.loading}`}>
                        <div className={styles.messageHeader}>
                            <span className={styles.messageAvatar}>🤖</span>
                        </div>
                        <div className={styles.messageContent}>
                            <span className={styles.loadingDots}>A processar<span>.</span><span>.</span><span>.</span></span>
                        </div>
                    </div>
                )}
                <div ref={historyEndRef} />
            </div>

            {!clarificationData && (
                <InputTextarea
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    rows={4}
                    autoResize
                    placeholder="Escreva a sua query (ex: 'mostrar eucalipto com área > 5 ha')"
                    style={{ width: "100%" }}
                    disabled={isProcessing}
                />
            )}

            {clarificationData && (
                <div className={styles.clarificationPanel}>
                    <span className={styles.clarificationQuestion}>{clarificationData.question}</span>

                    {clarificationData.type === 'multi_choice' && (
                        <div className={styles.multiChoiceContainer}>
                            {clarificationData.options?.map((opt) => {
                                const isAll = isAllOption(opt);
                                return (
                                    <div key={opt.id} className={styles.optionItem}>
                                        <Checkbox
                                            inputId={opt.id}
                                            checked={selectedOptions.includes(opt.value)}
                                            onChange={(e) => handleMultiChoiceChange(opt.value, e.checked || false)}
                                            disabled={useFreeText || isProcessing}
                                        />
                                        <label htmlFor={opt.id}>
                                            {isAll ? <strong>{opt.label}</strong> : opt.label}
                                        </label>
                                    </div>
                                );
                            })}
                            <div className={styles.freeTextOption}>
                                <Checkbox
                                    inputId="free-text"
                                    checked={useFreeText}
                                    onChange={(e) => {
                                        setUseFreeText(e.checked || false);
                                        if (e.checked) setSelectedOptions([]);
                                    }}
                                    disabled={isProcessing}
                                />
                                <label htmlFor="free-text">Outra (especificar)</label>
                                {useFreeText && (
                                    <InputTextarea
                                        value={freeTextValue}
                                        onChange={(e) => setFreeTextValue(e.target.value)}
                                        rows={2}
                                        autoResize
                                        placeholder="Escreva a sua resposta..."
                                        style={{ width: '100%', marginTop: '8px' }}
                                        disabled={isProcessing}
                                    />
                                )}
                            </div>
                        </div>
                    )}

                    {clarificationData.type === 'numeric_threshold' && (
                        <div className={styles.numericContainer}>
                            <div className={styles.rangeInputs}>
                                <span>De</span>
                                <InputNumber
                                    value={lowerBound}
                                    onValueChange={(e) => {
                                        const val = e.value ?? 0;
                                        setLowerBound(val);
                                        if (upperBound !== null && val > upperBound) setUpperBound(val);
                                    }}
                                    min={0}
                                    max={MAX_SLIDER_VALUE}
                                    step={0.01}
                                    suffix={clarificationData.unit ? ` ${clarificationData.unit}` : ''}
                                    style={{ width: '120px' }}
                                    disabled={isProcessing}
                                />
                                <span>a</span>
                                <InputNumber
                                    value={upperBound}
                                    onValueChange={(e) => setUpperBound(e.value ?? null)}
                                    min={lowerBound}
                                    max={MAX_SLIDER_VALUE}
                                    step={0.01}
                                    suffix={clarificationData.unit ? ` ${clarificationData.unit}` : ''}
                                    style={{ width: '120px' }}
                                    placeholder="sem limite"
                                    disabled={isProcessing}
                                />
                                {clarificationData.unit && <span>(em {clarificationData.unit})</span>}
                            </div>

                            <div className={styles.sliderContainer}>
                                <Slider
                                    value={[lowerBound, upperBound ?? MAX_SLIDER_VALUE]}
                                    onChange={(e) => {
                                        const values = e.value as [number, number];
                                        setLowerBound(values[0]);
                                        if (values[1] >= MAX_SLIDER_VALUE) {
                                            setUpperBound(null);
                                        } else {
                                            setUpperBound(values[1]);
                                        }
                                    }}
                                    min={0}
                                    max={MAX_SLIDER_VALUE}
                                    step={0.01}
                                    range
                                    className={styles.slider}
                                    disabled={isProcessing}
                                />
                                <div className={styles.sliderLabels}>
                                    <span>{formatNumber(lowerBound)}{clarificationData.unit ? ` ${clarificationData.unit}` : ''}</span>
                                    <span className={upperBound === null ? styles.noLimitLabel : ''}>
                                        {upperBound !== null
                                            ? `${formatNumber(upperBound)}${clarificationData.unit ? ` ${clarificationData.unit}` : ''}`
                                            : 'sem limite'}
                                    </span>
                                </div>
                            </div>
                            <div className={styles.rangeHint}>
                                <small>Arraste os pontos para definir o intervalo ou escreva os valores manualmente</small>
                            </div>
                        </div>
                    )}

                    {clarificationData.type === 'free_text' && (
                        <div className={styles.freeTextContainer}>
                            <InputTextarea
                                value={userResponse}
                                onChange={(e) => setUserResponse(e.target.value)}
                                rows={3}
                                autoResize
                                placeholder="Escreva a sua resposta..."
                                style={{ width: '100%' }}
                                disabled={isProcessing}
                            />
                        </div>
                    )}
                </div>
            )}

            <div className={styles.actions}>
                {clarificationData && (
                    <Button
                        label="Cancelar"
                        severity="secondary"
                        outlined
                        disabled={isProcessing}
                        onClick={resetClarification}
                    />
                )}
                <Button
                    label={clarificationData ? "Enviar resposta" : "Executar"}
                    disabled={!scenarioInfo || isProcessing || (clarificationData ? !isResponseValid() : !query.trim())}
                    loading={isProcessing}
                    onClick={execute}
                />
            </div>
        </div>
    );
}