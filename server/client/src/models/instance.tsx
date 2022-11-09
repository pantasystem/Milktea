import { z } from "zod";

const InstanceSchema = z.object({
  id: z.string(),
  host: z.string(),
  publishedAt: z.date().nullable(),
  clientMaxBodyByteSize: z.number().nullable(),
  deletedAt: z.date().nullable(),
  createdAt: z.date(),
  updatedAt: z.date()
});

type Instance = z.infer<typeof InstanceSchema>;

export default Instance;
export {InstanceSchema};