import { create } from 'zustand';

interface PictureUploadDraftState {
  singleFile: File | null;
  singlePreview: string;
  singleName: string;
  singleIntro: string;
  singleCategory: string;
  singleTags: string[];
  tagInput: string;
  isGenerating: boolean;
  aiTaskId: string | null;
}

interface PictureUploadStore extends PictureUploadDraftState {
  setDraft: (draft: Partial<PictureUploadDraftState>) => void;
  resetDraft: () => void;
}

function createDefaultDraftState(): PictureUploadDraftState {
  return {
    singleFile: null,
    singlePreview: '',
    singleName: '',
    singleIntro: '',
    singleCategory: '',
    singleTags: [],
    tagInput: '',
    isGenerating: false,
    aiTaskId: null,
  };
}

export const usePictureUploadStore = create<PictureUploadStore>((set) => ({
  ...createDefaultDraftState(),
  setDraft: (draft) => set(draft),
  resetDraft: () => set(createDefaultDraftState()),
}));
